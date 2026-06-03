# Kibana Queries

These are the Kibana Discover queries I use to check logs for the Spring Boot services in this project.

Data view:

```text
employee-leave-tracker-*
```

Useful fields:

- `container.name`
- `message`
- `project.name`

## All Java Services

```kql
container.name : ("api-gateway" or "auth-service" or "employee-service" or "leave-service" or "notification-service" or "discovery-service")
```

## Individual Services

API Gateway:

```kql
container.name : "api-gateway"
```

Auth Service:

```kql
container.name : "auth-service"
```

Employee Service:

```kql
container.name : "employee-service"
```

Leave Service:

```kql
container.name : "leave-service"
```

Notification Service:

```kql
container.name : "notification-service"
```

Discovery Service:

```kql
container.name : "discovery-service"
```

## Useful Scenario Queries

Login logs:

```kql
container.name : "auth-service" and message : "login"
```

Leave application logs:

```kql
container.name : "leave-service" and message : "leave apply"
```

Leave balance logs:

```kql
container.name : "employee-service" and message : "balance"
```

Gateway or JWT-related logs:

```kql
container.name : "api-gateway" and message : "JWT"
```

Errors across all Java services:

```kql
container.name : ("api-gateway" or "auth-service" or "employee-service" or "leave-service" or "notification-service" or "discovery-service") and message : ("ERROR" or "Exception" or "failed")
```
