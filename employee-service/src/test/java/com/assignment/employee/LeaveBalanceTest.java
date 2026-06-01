package com.assignment.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class LeaveBalanceTest {
    @Test
    void deductsFromRemainingBalance() {
        LeaveBalance balance = new LeaveBalance(201L, "CASUAL", 12);
        balance.deduct(2);
        assertThat(balance.getRemainingLeaves()).isEqualTo(10);
    }

    @Test
    void rejectsOverDeduction() {
        LeaveBalance balance = new LeaveBalance(201L, "SICK", 10);
        assertThatThrownBy(() -> balance.deduct(11))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
