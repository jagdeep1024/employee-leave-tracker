# Docker Images

This project is configured to run through Docker Compose. The Compose file uses published images for the Spring Boot services and UI, along with standard infrastructure images.

## Application Images

| Service | Docker Image |
| --- | --- |
| discovery-service | `jagcoool/nagp-employee-leave-tracker-discovery-service:latest` |
| api-gateway | `jagcoool/nagp-employee-leave-tracker-api-gateway:latest` |
| auth-service | `jagcoool/nagp-employee-leave-tracker-auth-service:latest` |
| employee-service | `jagcoool/nagp-employee-leave-tracker-employee-service:latest` |
| leave-service | `jagcoool/nagp-employee-leave-tracker-leave-service:latest` |
| notification-service | `jagcoool/nagp-employee-leave-tracker-notification-service:latest` |
| ui | `jagcoool/nagp-employee-leave-tracker-ui:latest` |

## Infrastructure Images

| Component | Image |
| --- | --- |
| PostgreSQL | `postgres:16-alpine` |
| RabbitMQ | `rabbitmq:3.13-management-alpine` |
| Zipkin | `openzipkin/zipkin:2` |
| Elasticsearch | `docker.elastic.co/elasticsearch/elasticsearch:8.15.3` |
| Kibana | `docker.elastic.co/kibana/kibana:8.15.3` |
| Filebeat | `docker.elastic.co/beats/filebeat:8.15.3` |

## Running The Images

The judges do not need to run each image manually. The expected startup command is:

```bash
docker compose up -d
```

Docker Compose starts the application services, PostgreSQL, RabbitMQ, Eureka, Zipkin, Elasticsearch, Kibana, and Filebeat with the required environment variables and ports.

## Notes

- `docker-compose.yml` is the source of truth for image names, ports, and environment variables.
- The application uses the `latest` tag for demo simplicity.
- PostgreSQL data is stored in a Docker volume.
- Use `docker compose down -v` only when fresh seeded data is required.
