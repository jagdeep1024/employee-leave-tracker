package com.assignment.leave;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LeaveController {
    private final LeaveRequestRepository leaves;
    private final EmployeeClient employeeClient;
    private final RabbitTemplate rabbitTemplate;

    public LeaveController(LeaveRequestRepository leaves, EmployeeClient employeeClient, RabbitTemplate rabbitTemplate) {
        this.leaves = leaves;
        this.employeeClient = employeeClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/leaves")
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveRequest apply(@RequestBody ApplyLeaveRequest request,
                              @RequestHeader("X-User-Id") Long userId,
                              @RequestHeader("X-User-Role") String role) {
        if (!"EMPLOYEE".equals(role)) {
            throw new ForbiddenException("Only employees can apply for leave");
        }
        validateApplication(request, userId);
        LeaveRequest leave = leaves.save(new LeaveRequest(userId, request.managerId(), request.leaveType().toUpperCase(),
                request.startDate(), request.endDate(), request.numberOfDays(), request.reason()));
        publish("LEAVE_APPLIED", leave.getEmployeeId(), leave.getManagerId(), leave.getId(),
                "Leave request submitted for approval");
        return leave;
    }

    @GetMapping("/leaves/history")
    public Page<LeaveRequest> history(@RequestParam(value = "status", required = false) LeaveStatus status,
                                      @RequestParam(value = "page", defaultValue = "0") int page,
                                      @RequestParam(value = "size", defaultValue = "10") int size,
                                      @RequestHeader("X-User-Id") Long userId,
                                      @RequestHeader("X-User-Role") String role) {
        if (!"EMPLOYEE".equals(role)) {
            throw new ForbiddenException("Only employees can view employee leave history");
        }
        return leaves.history(userId, status, PageRequest.of(page, size));
    }

    @GetMapping("/manager/leaves")
    public List<LeaveRequest> managerLeaves(@RequestParam(value = "status", required = false) LeaveStatus status,
                                            @RequestParam(value = "employeeId", required = false) Long employeeId,
                                            @RequestParam(value = "from", required = false) LocalDate from,
                                            @RequestParam(value = "to", required = false) LocalDate to,
                                            @RequestHeader("X-User-Id") Long userId,
                                            @RequestHeader("X-User-Role") String role) {
        if (!"MANAGER".equals(role)) {
            throw new ForbiddenException("Only managers can view team leave requests");
        }
        return leaves.managerSearch(userId, status, employeeId, from, to);
    }

    @Transactional
    @PostMapping("/manager/leaves/{leaveId}/approve")
    public LeaveRequest approve(@PathVariable("leaveId") Long leaveId,
                                @RequestHeader("X-User-Id") Long userId,
                                @RequestHeader("X-User-Role") String role) {
        if (!"MANAGER".equals(role)) {
            throw new ForbiddenException("Only managers can approve leave");
        }
        LeaveRequest leave = findManagerLeave(leaveId, userId);
        employeeClient.deduct(leave.getEmployeeId(), leave.getLeaveType(), leave.getNumberOfDays());
        leave.approve();
        publish("LEAVE_APPROVED", leave.getEmployeeId(), leave.getManagerId(), leave.getId(), "Leave request approved");
        return leave;
    }

    @Transactional
    @PostMapping("/manager/leaves/{leaveId}/reject")
    public LeaveRequest reject(@PathVariable("leaveId") Long leaveId,
                               @RequestBody RejectRequest request,
                               @RequestHeader("X-User-Id") Long userId,
                               @RequestHeader("X-User-Role") String role) {
        if (!"MANAGER".equals(role)) {
            throw new ForbiddenException("Only managers can reject leave");
        }
        LeaveRequest leave = findManagerLeave(leaveId, userId);
        leave.reject(request.reason());
        publish("LEAVE_REJECTED", leave.getEmployeeId(), leave.getManagerId(), leave.getId(),
                "Leave request rejected: " + request.reason());
        return leave;
    }

    private void validateApplication(ApplyLeaveRequest request, Long employeeId) {
        if (request.startDate() == null || request.endDate() == null || request.startDate().isAfter(request.endDate())) {
            throw new IllegalArgumentException("Invalid date range");
        }
        if (request.startDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Leave cannot be applied for past dates");
        }
        if (request.numberOfDays() <= 0) {
            throw new IllegalArgumentException("Number of days must be greater than zero");
        }
        if (!employeeClient.managerValid(employeeId, request.managerId())) {
            throw new IllegalArgumentException("Reporting manager is not valid for this employee");
        }
        EmployeeClient.BalanceResponse balance = employeeClient.balance(employeeId, request.leaveType());
        if (balance.remainingLeaves() < request.numberOfDays()) {
            throw new IllegalArgumentException("Insufficient leave balance");
        }
        if (leaves.hasOverlap(employeeId, request.startDate(), request.endDate())) {
            throw new ConflictException("A pending or approved leave already exists for this date range");
        }
    }

    private LeaveRequest findManagerLeave(Long leaveId, Long managerId) {
        LeaveRequest leave = leaves.findById(leaveId).orElseThrow(() -> new NotFoundException("Leave request not found"));
        if (!managerId.equals(leave.getManagerId())) {
            throw new ForbiddenException("Managers can act only on their team leave requests");
        }
        return leave;
    }

    private void publish(String type, Long employeeId, Long managerId, Long leaveId, String message) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY,
                Map.of("type", type, "employeeId", employeeId, "managerId", managerId,
                        "leaveId", leaveId, "message", message));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    ErrorResponse notFound(Exception ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException.class)
    ErrorResponse forbidden(Exception ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({ConflictException.class, IllegalStateException.class})
    ErrorResponse conflict(Exception ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    ErrorResponse badRequest(Exception ex) {
        return new ErrorResponse(ex.getMessage());
    }

    record ApplyLeaveRequest(String leaveType, LocalDate startDate, LocalDate endDate,
                             double numberOfDays, String reason, Long managerId) {
    }

    record RejectRequest(String reason) {
    }

    record ErrorResponse(String message) {
    }

    static class NotFoundException extends RuntimeException {
        NotFoundException(String message) {
            super(message);
        }
    }

    static class ForbiddenException extends RuntimeException {
        ForbiddenException(String message) {
            super(message);
        }
    }

    static class ConflictException extends RuntimeException {
        ConflictException(String message) {
            super(message);
        }
    }
}
