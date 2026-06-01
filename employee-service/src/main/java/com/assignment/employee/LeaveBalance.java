package com.assignment.employee;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "leave_balances", uniqueConstraints = @UniqueConstraint(columnNames = {"employeeId", "leaveType"}))
public class LeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long employeeId;
    private String leaveType;
    private double totalAllocated;
    private double usedLeaves;

    protected LeaveBalance() {
    }

    public LeaveBalance(Long employeeId, String leaveType, double totalAllocated) {
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.totalAllocated = totalAllocated;
        this.usedLeaves = 0;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public double getTotalAllocated() {
        return totalAllocated;
    }

    public double getUsedLeaves() {
        return usedLeaves;
    }

    public double getRemainingLeaves() {
        return totalAllocated - usedLeaves;
    }

    public void deduct(double days) {
        if (days > getRemainingLeaves()) {
            throw new IllegalArgumentException("Insufficient leave balance");
        }
        this.usedLeaves += days;
    }
}
