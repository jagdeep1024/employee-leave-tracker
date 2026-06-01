package com.assignment.leave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LeaveRequestTest {
    @Test
    void startsAsPendingAndCanApprove() {
        LeaveRequest leave = new LeaveRequest(201L, 101L, "CASUAL",
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(2), 1, "Personal work");
        leave.approve();
        assertThat(leave.getStatus()).isEqualTo(LeaveStatus.APPROVED);
    }

    @Test
    void approvedLeaveCannotBeRejected() {
        LeaveRequest leave = new LeaveRequest(201L, 101L, "CASUAL",
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(2), 1, "Personal work");
        leave.approve();
        assertThatThrownBy(() -> leave.reject("No"))
                .isInstanceOf(IllegalStateException.class);
    }
}
