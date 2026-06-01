package com.assignment.employee;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    private Long id;
    private String name;
    private String email;
    private String role;
    private Long managerId;

    protected Employee() {
    }

    public Employee(Long id, String name, String email, String role, Long managerId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.managerId = managerId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Long getManagerId() {
        return managerId;
    }
}
