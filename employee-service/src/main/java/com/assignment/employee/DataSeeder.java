package com.assignment.employee;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final EmployeeRepository employees;
    private final LeaveBalanceRepository balances;

    public DataSeeder(EmployeeRepository employees, LeaveBalanceRepository balances) {
        this.employees = employees;
        this.balances = balances;
    }

    @Override
    public void run(String... args) {
        if (employees.count() > 0) {
            return;
        }
        employees.saveAll(List.of(
                new Employee(101L, "Meenal Garg", "manager1@company.com", "MANAGER", null),
                new Employee(102L, "Archit bansal", "manager2@company.com", "MANAGER", null),
                new Employee(201L, "Jagdeep Employee", "employee1@company.com", "EMPLOYEE", 101L),
                new Employee(202L, "Arjun Employee", "employee2@company.com", "EMPLOYEE", 101L),
                new Employee(203L, "Meera Employee", "employee3@company.com", "EMPLOYEE", 102L)
        ));

        for (Long employeeId : List.of(201L, 202L, 203L)) {
            balances.saveAll(List.of(
                    new LeaveBalance(employeeId, "CASUAL", 12),
                    new LeaveBalance(employeeId, "SICK", 10),
                    new LeaveBalance(employeeId, "PRIVILEGE", 15)
            ));
        }
    }
}
