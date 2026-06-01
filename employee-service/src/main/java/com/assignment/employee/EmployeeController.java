package com.assignment.employee;

import jakarta.transaction.Transactional;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeRepository employees;
    private final LeaveBalanceRepository balances;

    public EmployeeController(EmployeeRepository employees, LeaveBalanceRepository balances) {
        this.employees = employees;
        this.balances = balances;
    }

    @GetMapping("/me")
    public Employee me(@RequestHeader("X-User-Id") Long userId) {
        log.info("employee me requested for user {}", userId);
        return findEmployee(userId);
    }

    @GetMapping("/{id}")
    public Employee byId(@PathVariable("id") Long id,
                         @RequestHeader("X-User-Id") Long userId,
                         @RequestHeader("X-User-Role") String role) {
        log.info("employee profile requested for {} by {} ({})", id, userId, role);
        ensureAllowed(id, userId, role);
        return findEmployee(id);
    }

    @GetMapping("/{id}/leave-balances")
    public List<BalanceResponse> balances(@PathVariable("id") Long id,
                                           @RequestHeader("X-User-Id") Long userId,
                                           @RequestHeader("X-User-Role") String role) {
        log.info("leave balances requested for {} by {} ({})", id, userId, role);
        ensureAllowed(id, userId, role);
        return balances.findByEmployeeId(id).stream().map(BalanceResponse::from).toList();
    }

    @GetMapping("/manager/{managerId}/team")
    public List<Employee> team(@PathVariable("managerId") Long managerId,
                               @RequestHeader("X-User-Id") Long userId,
                               @RequestHeader("X-User-Role") String role) {
        log.info("team lookup requested for manager {} by {} ({})", managerId, userId, role);
        if (!"MANAGER".equals(role) || !managerId.equals(userId)) {
            throw new ForbiddenException("Managers can view only their own team");
        }
        return employees.findByManagerId(managerId);
    }

    @GetMapping("/internal/{id}/manager/{managerId}/valid")
    public ValidResponse managerValid(@PathVariable("id") Long id,
                                      @PathVariable("managerId") Long managerId) {
        log.info("manager validity check for employee {} and manager {}", id, managerId);
        Employee employee = findEmployee(id);
        return new ValidResponse(managerId.equals(employee.getManagerId()));
    }

    @GetMapping("/internal/{id}/balance/{leaveType}")
    public BalanceResponse balanceForLeave(@PathVariable("id") Long id,
                                           @PathVariable("leaveType") String leaveType) {
        log.info("internal balance lookup for employee {} leaveType {}", id, leaveType);
        return balances.findByEmployeeIdAndLeaveTypeIgnoreCase(id, leaveType)
                .map(BalanceResponse::from)
                .orElseThrow(() -> new NotFoundException("Leave balance not found"));
    }

    @Transactional
    @PostMapping("/internal/{id}/balance/{leaveType}/deduct")
    public BalanceResponse deduct(@PathVariable("id") Long id,
                                  @PathVariable("leaveType") String leaveType,
                                  @RequestParam("days") double days) {
        log.info("internal balance deduct for employee {} leaveType {} days {}", id, leaveType, days);
        LeaveBalance balance = balances.findByEmployeeIdAndLeaveTypeIgnoreCase(id, leaveType)
                .orElseThrow(() -> new NotFoundException("Leave balance not found"));
        balance.deduct(days);
        return BalanceResponse.from(balance);
    }

    private Employee findEmployee(Long id) {
        return employees.findById(id).orElseThrow(() -> new NotFoundException("Employee not found"));
    }

    private void ensureAllowed(Long employeeId, Long userId, String role) {
        if (employeeId.equals(userId)) {
            return;
        }
        if ("MANAGER".equals(role)) {
            Employee employee = findEmployee(employeeId);
            if (userId.equals(employee.getManagerId())) {
                return;
            }
        }
        throw new ForbiddenException("You are not allowed to access this employee");
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    ErrorResponse badRequest(Exception ex) {
        return new ErrorResponse(ex.getMessage());
    }

    record BalanceResponse(String leaveType, double totalAllocated, double usedLeaves, double remainingLeaves) {
        static BalanceResponse from(LeaveBalance balance) {
            return new BalanceResponse(balance.getLeaveType(), balance.getTotalAllocated(),
                    balance.getUsedLeaves(), balance.getRemainingLeaves());
        }
    }

    record ValidResponse(boolean valid) {
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
}
