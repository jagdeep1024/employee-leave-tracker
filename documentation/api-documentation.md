# API Documentation

All API calls for demo and judging should go through the API Gateway.

Base URL:

```text
http://localhost:9000
```

Protected APIs require:

```http
Authorization: Bearer <jwt>
```

## Auth

### POST /auth/login

This endpoint authenticates one of the seeded users and returns a JWT token.

Request:

```json
{
  "email": "employee1@company.com",
  "password": "password"
}
```

Response:

```json
{
  "token": "jwt-token",
  "userId": 201,
  "name": "Jagdeep Employee",
  "email": "employee1@company.com",
  "role": "EMPLOYEE",
  "expiresInSeconds": 28800
}
```

Manager users:

| Name | Email | Password | User Id |
| --- | --- | --- | --- |
| Meenal Garg | manager1@company.com | password | 101 |
| Archit bansal | manager2@company.com | password | 102 |

## Employees

### GET /employees/me

Returns the logged-in user's profile.

### GET /employees/{id}

Access rules:

- Employee can access only self.
- Manager can access only assigned team members.

### GET /employees/{id}/leave-balances

Returns leave balance by leave type.

Response:

```json
[
  {
    "leaveType": "CASUAL",
    "totalAllocated": 12,
    "usedLeaves": 0,
    "remainingLeaves": 12
  },
  {
    "leaveType": "SICK",
    "totalAllocated": 10,
    "usedLeaves": 0,
    "remainingLeaves": 10
  },
  {
    "leaveType": "PRIVILEGE",
    "totalAllocated": 15,
    "usedLeaves": 0,
    "remainingLeaves": 15
  }
]
```

### GET /employees/manager/{managerId}/team

Manager-only endpoint to list team members.

Example:

```text
GET /employees/manager/101/team
```

## Leaves

### POST /leaves

Employee-only endpoint for applying leave.

Request:

```json
{
  "leaveType": "CASUAL",
  "startDate": "2026-06-10",
  "endDate": "2026-06-11",
  "numberOfDays": 2,
  "reason": "Family work",
  "managerId": 101
}
```

Response status: `201 Created`

Validations implemented:

- Past dates are not allowed.
- Start date must be before or equal to end date.
- Employee must have sufficient leave balance.
- Pending or approved overlapping leave is blocked.
- Reporting manager must match the employee's assigned manager.

### GET /leaves/history?status=&page=&size=

Employee-only paginated leave history.

Supported status filters:

- `PENDING`
- `APPROVED`
- `REJECTED`

### GET /manager/leaves?status=&employeeId=&from=&to=

Manager-only endpoint for viewing team leave requests.

Supported filters:

- status
- employee id
- date range

### POST /manager/leaves/{leaveId}/approve

Manager-only endpoint. It approves a pending leave request and deducts the employee's leave balance.

### POST /manager/leaves/{leaveId}/reject

Manager-only endpoint. It rejects a pending leave request and stores the rejection reason.

Request:

```json
{
  "reason": "Team coverage issue"
}
```

## Notifications

### GET /notifications/me

Returns notification records for the logged-in user.

Notifications are created from RabbitMQ events such as:

- `LEAVE_APPLIED`
- `LEAVE_APPROVED`
- `LEAVE_REJECTED`

## Status Codes

| Code | Meaning |
| --- | --- |
| 200 | Request completed |
| 201 | Resource created |
| 400 | Validation error |
| 401 | Missing or invalid token |
| 403 | Role or ownership violation |
| 404 | Resource not found |
| 409 | Leave overlap or invalid status transition |
| 500 | Unexpected server error |
