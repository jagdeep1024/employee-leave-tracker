# Setup Guide

This guide explains how I run the Employee Leave Tracker project for evaluation and demo purposes.

## Prerequisites

- Docker Desktop
- Java 17, only for local Maven build/testing
- Maven, only for local Maven build/testing

The project is intended to start with Docker Compose, so Java and Maven are not required just to run the containers.

## Repository

Public GitHub repository:

```text
https://github.com/jagdeep1024/employee-leave-tracker
```

## Fresh Server Setup

On a fresh Ubuntu server, install Git and Docker:

```bash
sudo apt update
sudo apt install -y git docker.io docker-compose-v2
sudo systemctl enable --now docker
```

Clone the repository:

```bash
cd /home
sudo git clone https://github.com/jagdeep1024/employee-leave-tracker.git
sudo chown -R $USER:$USER employee-leave-tracker
cd employee-leave-tracker
```

Start the full stack:

```bash
docker compose up -d
```

Check containers:

```bash
docker compose ps
```

Check logs if any service is not healthy:

```bash
docker compose logs -f
```

## Start The Project

From the project root:

```bash
docker compose up -d
```

On Windows, I have also kept a small helper script:

```powershell
.\start.ps1
```

Both commands start the same Docker Compose stack.

## Stop The Project

```bash
docker compose down
```

To reset database data and seed fresh records again:

```bash
docker compose down -v
```

I use `docker compose down -v` only when I want to clear old PostgreSQL volume data.

## Ports

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

RabbitMQ credentials: `leave_user` / `leave_pass`

PostgreSQL credentials:

```text
database: leave_tracker
username: leave_user
password: leave_pass
```

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

## Environment Variables

| Variable | Purpose |
| --- | --- |
| `SERVER_PORT` | Port used by each Spring Boot service |
| `JWT_SECRET` | JWT signing key shared by auth-service and gateway |
| `DB_URL` | PostgreSQL JDBC URL |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `RABBIT_HOST` | RabbitMQ host |
| `RABBIT_PORT` | RabbitMQ AMQP port |
| `RABBIT_USERNAME` | RabbitMQ username |
| `RABBIT_PASSWORD` | RabbitMQ password |
| `EUREKA_URL` | Eureka registry URL |
| `ZIPKIN_URL` | Zipkin collector endpoint |
| `EMPLOYEE_SERVICE_URL` | Direct URL used by leave-service for employee-service calls |
| `CORS_ALLOWED_ORIGINS` | UI origin allowed by the API Gateway |

## Local Maven Build

Run all tests:

```bash
mvn clean test
```

Package all modules:

```bash
mvn clean package
```

Package one service with dependencies:

```bash
mvn -pl leave-service -am package
```
