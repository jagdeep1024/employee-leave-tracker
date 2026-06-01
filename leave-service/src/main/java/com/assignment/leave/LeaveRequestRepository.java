package com.assignment.leave;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    @Query("""
            select l from LeaveRequest l
            where l.employeeId = :employeeId
              and (:status is null or l.status = :status)
            order by l.createdAt desc
            """)
    Page<LeaveRequest> history(@Param("employeeId") Long employeeId,
                               @Param("status") LeaveStatus status,
                               Pageable pageable);

    @Query("""
            select count(l) > 0 from LeaveRequest l
            where l.employeeId = :employeeId
              and l.status in (com.assignment.leave.LeaveStatus.PENDING, com.assignment.leave.LeaveStatus.APPROVED)
              and l.startDate <= :endDate
              and l.endDate >= :startDate
            """)
    boolean hasOverlap(Long employeeId, LocalDate startDate, LocalDate endDate);

    @Query("""
            select l from LeaveRequest l
            where l.managerId = :managerId
              and (:status is null or l.status = :status)
              and (:employeeId is null or l.employeeId = :employeeId)
              and (:fromDate is null or l.endDate >= :fromDate)
              and (:toDate is null or l.startDate <= :toDate)
            order by l.createdAt desc
            """)
    List<LeaveRequest> managerSearch(Long managerId, LeaveStatus status, Long employeeId,
                                     LocalDate fromDate, LocalDate toDate);
}
