package com.assignment.leave;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long employeeId;
    private Long managerId;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private double numberOfDays;
    private String reason;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
    private String rejectionReason;
    private OffsetDateTime createdAt;

    protected LeaveRequest() {
    }

    public LeaveRequest(Long employeeId, Long managerId, String leaveType, LocalDate startDate,
                        LocalDate endDate, double numberOfDays, String reason) {
        this.employeeId = employeeId;
        this.managerId = managerId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfDays = numberOfDays;
        this.reason = reason;
        this.status = LeaveStatus.PENDING;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public Long getManagerId() {
        return managerId;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public double getNumberOfDays() {
        return numberOfDays;
    }

    public String getReason() {
        return reason;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void approve() {
        if (status != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only pending leave requests can be approved");
        }
        status = LeaveStatus.APPROVED;
    }

    public void reject(String reason) {
        if (status != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only pending leave requests can be rejected");
        }
        status = LeaveStatus.REJECTED;
        rejectionReason = reason;
    }
}
