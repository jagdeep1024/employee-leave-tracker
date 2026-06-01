package com.assignment.employee;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    List<LeaveBalance> findByEmployeeId(Long employeeId);

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIgnoreCase(Long employeeId, String leaveType);
}
