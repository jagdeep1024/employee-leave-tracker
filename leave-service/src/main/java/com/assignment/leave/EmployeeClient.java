package com.assignment.leave;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class EmployeeClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String employeeServiceUrl;

    public EmployeeClient(@Value("${app.employee-service-url}") String employeeServiceUrl) {
        this.employeeServiceUrl = employeeServiceUrl;
    }

    @CircuitBreaker(name = "employeeService")
    public BalanceResponse balance(Long employeeId, String leaveType) {
        try {
            return restTemplate.getForObject(employeeServiceUrl + "/employees/internal/" + employeeId
                    + "/balance/" + leaveType, BalanceResponse.class);
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Unable to verify leave balance");
        }
    }

    @CircuitBreaker(name = "employeeService")
    public boolean managerValid(Long employeeId, Long managerId) {
        try {
            ValidResponse response = restTemplate.getForObject(employeeServiceUrl + "/employees/internal/" + employeeId
                    + "/manager/" + managerId + "/valid", ValidResponse.class);
            return response != null && response.valid();
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Unable to verify reporting manager");
        }
    }

    @CircuitBreaker(name = "employeeService")
    public void deduct(Long employeeId, String leaveType, double days) {
        try {
            restTemplate.postForObject(employeeServiceUrl + "/employees/internal/" + employeeId
                    + "/balance/" + leaveType + "/deduct?days=" + days, null, BalanceResponse.class);
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Unable to deduct leave balance");
        }
    }

    record BalanceResponse(String leaveType, double totalAllocated, double usedLeaves, double remainingLeaves) {
    }

    record ValidResponse(boolean valid) {
    }
}
