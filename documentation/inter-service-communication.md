# Inter-Service Communication

This document explains how I handled communication between services in the Employee Leave Tracker project.

## Gateway Routing

The API Gateway is the public backend entry point. Browser, Postman, and demo traffic should use:

```text
http://localhost:9000
```

The gateway validates JWT tokens and forwards requests to the correct service.

Main routes:

- `/auth/**` -> `auth-service`
- `/employees/**` -> `employee-service`
- `/leaves/**` -> `leave-service`
- `/manager/leaves/**` -> `leave-service`
- `/notifications/**` -> `notification-service`

## Synchronous Calls

`leave-service` calls `employee-service` directly for leave validations and balance updates.

These calls are required for:

- validating the reporting manager
- checking leave balance
- deducting leave balance after approval

The calls are protected with a Resilience4j circuit breaker named:

```text
employeeService
```

If `employee-service` is unavailable, `leave-service` does not silently approve leave. It returns a validation-style error so incorrect leave data is not saved.

## Asynchronous Calls

RabbitMQ is used for notification events. This keeps the leave workflow separate from notification persistence.

RabbitMQ configuration:

```text
exchange: leave.events
queue: notification.events
routing key: leave.notification
```

Example event:

```json
{
  "type": "LEAVE_APPLIED",
  "employeeId": 201,
  "managerId": 101,
  "leaveId": 1,
  "message": "Leave request submitted for approval"
}
```

Event types:

- `LEAVE_APPLIED`
- `LEAVE_APPROVED`
- `LEAVE_REJECTED`

## Assumptions

- Notification means stored/logged notification records, not email or SMS.
- PostgreSQL is shared in Docker Compose to keep the project easy to run for evaluation.
- Each service writes only to its own tables.
- Seeded users are enough for demo and testing.
- API testing should be done through the gateway, not by directly calling every service port.

## Failure Handling

- Invalid JWT is rejected at gateway with `401`.
- Invalid ownership or role access returns `403`.
- Leave validation failures return `400` or `409`.
- Employee-service failures are handled by leave-service through the circuit breaker path.
- RabbitMQ is used for async notification flow, so notification handling is separated from the main leave request transaction.
