# notification-service

Consomme les événements Kafka et envoie emails / rappels / notifications in-app.

| | |
|---|---|
| **Port** | `8084` |
| **Base** | PostgreSQL - `juribook_notification` |
| **Stack** | Java 21 · Spring Boot 4 · Apache Kafka · Spring Mail · Thymeleaf · Flyway |

## Lancer en local

```bash
mvn spring-boot:run
```

Swagger : http://localhost:8084/swagger-ui.html