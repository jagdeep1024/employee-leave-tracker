# Employee Leave Tracker

Employee Leave Tracker is a microservices-based backend project for managing employee leave requests. It covers employee login, leave balance lookup, leave application, manager approval or rejection, leave history, and notifications.

This project was built for the Microservices Assignment 2026 using Java Spring Boot, Docker Compose, RabbitMQ, Eureka, API Gateway, PostgreSQL, Zipkin, and ELK-based log viewing.

## Try The Live Hosted Version

I have deployed a live running version of this project on my personal VPS for direct evaluation.

Demo video:

```text
https://drive.google.com/drive/folders/1Kam0RSMFbvBLdFt556VVRdfXLkyiKUG_?usp=sharing
```

| Component | Live URL |
| --- | --- |
| UI | http://140.245.12.187:9006 |
| API Gateway | http://140.245.12.187:9000 |
| Eureka | http://140.245.12.187:9005 |
| RabbitMQ Management | http://140.245.12.187:9008 |
| Zipkin | http://140.245.12.187:9010 |
| Elasticsearch | http://140.245.12.187:9011 |
| Kibana | http://140.245.12.187:9012 |

Demo login credentials:

| Role | Email | Password |
| --- | --- | --- |
| Manager | manager1@company.com | password |
| Employee | employee1@company.com | password |

## What This Project Covers

- JWT-based login and authentication
- Role-based access for employees and managers
- Employee leave balance by leave type
- Leave application with validations
- Manager approval and rejection flow
- Leave balance deduction after approval
- Notification logging through RabbitMQ
- Service discovery using Eureka
- API routing through Spring Cloud Gateway
- Circuit breaker for inter-service calls
- Distributed tracing using Zipkin
- Docker Compose based startup
- API documentation and Postman collection

## Project Structure

| Folder | Description |
| --- | --- |
| `auth-service` | Handles login and JWT token generation |
| `employee-service` | Maintains employee details, manager mapping, and leave balances |
| `leave-service` | Handles leave application, validation, history, approval, and rejection |
| `notification-service` | Consumes leave events and stores notification records |
| `api-gateway` | Main backend entry point for all API requests |
| `discovery-service` | Eureka service registry |
| `ui` | Simple browser UI for demonstration |
| `documentation` | API docs, architecture notes, setup guide, demo script, and Postman collection |
| `filebeat` | Ships Docker logs to Elasticsearch |
| `logstash` | Optional Logstash pipeline configuration |

## Technology Stack

- Java 17
- Spring Boot
- Spring Cloud Gateway
- Eureka Service Discovery
- Spring Data JPA
- PostgreSQL
- RabbitMQ
- Resilience4j
- Spring Actuator
- Zipkin
- Elasticsearch and Kibana
- Docker Compose
- HTML, CSS, and JavaScript UI

## How To Run

Start all services:

```bash
docker compose up -d
```

On Windows, the same command is available through:

```powershell
.\start.ps1
```

Stop all services:

```bash
docker compose down
```

Stop services and clear database volumes:

```bash
docker compose down -v
```

Use `docker compose down -v` only when fresh seed data is needed again.

## Application URLs

| Component | URL |
| --- | --- |
| UI | http://localhost:9006 |
| API Gateway | http://localhost:9000 |
| Auth Service | http://localhost:9001 |
| Employee Service | http://localhost:9002 |
| Leave Service | http://localhost:9003 |
| Notification Service | http://localhost:9004 |
| Eureka | http://localhost:9005 |
| RabbitMQ Management | http://localhost:9008 or http://localhost:15672 |
| Zipkin | http://localhost:9010 |
| PostgreSQL | localhost:5432 or localhost:9007 |
| RabbitMQ AMQP | localhost:5672 or localhost:9009 |
| Elasticsearch | http://localhost:9011 |
| Kibana | http://localhost:9012 |

RabbitMQ:

```text
username: leave_user
password: leave_pass
```

PostgreSQL:

```text
database: leave_tracker
username: leave_user
password: leave_pass
```

## Services

| Service | Port | Responsibility |
| --- | --- | --- |
| `api-gateway` | 9000 | Routes requests and validates JWT tokens |
| `auth-service` | 9001 | Authenticates users and creates JWT tokens |
| `employee-service` | 9002 | Handles employee records, teams, and leave balances |
| `leave-service` | 9003 | Handles leave requests and approval workflow |
| `notification-service` | 9004 | Stores notifications generated from leave events |
| `discovery-service` | 9005 | Registers and discovers services |
| `ui` | 9006 | Browser-based demo UI |

All API testing should be done through the gateway:

```text
http://localhost:9000
```

Direct service ports are useful for debugging, health checks, and actuator endpoints.

## Seeded Users

| Role | Name | Email | Password | User Id |
| --- | --- | --- | --- | --- |
| Manager | Meenal Garg | manager1@company.com | password | 101 |
| Manager | Archit bansal | manager2@company.com | password | 102 |
| Employee | Jagdeep Employee | employee1@company.com | password | 201 |
| Employee | Arjun Employee | employee2@company.com | password | 202 |
| Employee | Meera Employee | employee3@company.com | password | 203 |

Employee-manager mapping:

| Employee Id | Manager Id |
| --- | --- |
| 201 | 101 |
| 202 | 101 |
| 203 | 102 |

## Default Leave Allocation

When the employee data is seeded for the first time, default leave balances are created in the `leave_balances` table.

| Leave Type | Allocated Days |
| --- | --- |
| CASUAL | 12 |
| SICK | 10 |
| PRIVILEGE | 15 |

The seed logic runs only when the employee table is empty. If the database volume already exists, the old seeded records remain until the volume is reset or the rows are updated manually.

## API Testing

Login:

```powershell
$login = Invoke-RestMethod -Method Post http://localhost:9000/auth/login `
  -ContentType "application/json" `
  -Body '{"email":"employee1@company.com","password":"password"}'

$token = $login.token
```

Apply for leave:

```powershell
Invoke-RestMethod -Method Post http://localhost:9000/leaves `
  -Headers @{ Authorization = "Bearer $token" } `
  -ContentType "application/json" `
  -Body '{"leaveType":"CASUAL","startDate":"2026-06-10","endDate":"2026-06-11","numberOfDays":2,"reason":"Family work","managerId":101}'
```

Main API groups:

- `/auth/**`
- `/employees/**`
- `/leaves/**`
- `/manager/leaves/**`
- `/notifications/**`

Detailed request and response examples are available in:

- [API Documentation](documentation/api-documentation.md)
- [Postman Collection](documentation/postman-collection.json)

## Security

- Login is public through `POST /auth/login`.
- All other APIs require a bearer token.
- JWT contains user id, role, email, issue time, and expiry.
- API Gateway validates the token before forwarding requests.
- API Gateway forwards `X-User-Id` and `X-User-Role` headers to downstream services.
- Employees can access only their own data.
- Managers can access only their team members and assigned leave requests.

## Leave Workflow

1. Employee logs in.
2. Employee checks leave balance.
3. Employee applies for leave.
4. Leave service validates date range, manager mapping, balance, and overlapping requests.
5. Leave request is created with `PENDING` status.
6. Manager approves or rejects the request.
7. On approval, leave balance is deducted.
8. On approval or rejection, notification events are logged.

RabbitMQ configuration:

```text
exchange: leave.events
queue: notification.events
routing key: leave.notification
```

## Circuit Breaker

`leave-service` depends on `employee-service` for:

- Manager validation
- Leave balance check
- Leave balance deduction

These calls are protected using a Resilience4j circuit breaker named `employeeService`.

Configuration:

```properties
resilience4j.circuitbreaker.instances.employeeService.sliding-window-size=5
resilience4j.circuitbreaker.instances.employeeService.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.employeeService.wait-duration-in-open-state=10s
```

Actuator endpoint:

```text
http://localhost:9003/actuator/circuitbreakers/employeeService
```

## Observability

- Zipkin is available at http://localhost:9010 for distributed tracing.
- Spring Actuator exposes health and info endpoints.
- Filebeat ships Docker container logs to Elasticsearch.
- Kibana is available at http://localhost:9012 for log inspection.

Kibana data view:

```text
employee-leave-tracker-*
```

Useful fields while checking logs:

- `container.name`
- `message`
- `project.name`

Useful Kibana KQL query for all coded Java services:

```text
container.name : ("api-gateway" or "auth-service" or "employee-service" or "leave-service" or "notification-service" or "discovery-service")
```

More Kibana queries are available in [Kibana Queries](documentation/kibana-queries.md).

Logstash configuration is present but the Logstash service is not enabled by default in `docker-compose.yml`.

## Local Build

Run tests:

```bash
mvn clean test
```

Package all services:

```bash
mvn clean package
```

Package one service:

```bash
mvn -pl leave-service -am package
```

Run one service locally:

```bash
mvn -pl leave-service spring-boot:run
```

## Submission Documents

- [Architecture Design](documentation/architecture-design.md)
- [API Documentation](documentation/api-documentation.md)
- [Inter-Service Communication](documentation/inter-service-communication.md)
- [Docker Images](documentation/docker-images.md)
- [Setup Guide](documentation/setup-guide.md)
- [Demo Script](documentation/demo-script.md)
- [Kibana Queries](documentation/kibana-queries.md)
- [Postman Collection](documentation/postman-collection.json)

Demo video link: add final recording link here.
