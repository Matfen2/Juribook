# audit-service

Journal d'audit immutable, consomme tous les événements et les persiste en append-only.

| | |
|---|---|
| **Port** | `8085` |
| **Base** | PostgreSQL - `juribook_audit` |
| **Stack** | Java 21 · Spring Boot 4 · Apache Kafka · Spring Data JPA · Flyway |

## Lancer en local

```bash
mvn spring-boot:run
```

Swagger : http://localhost:8085/swagger-ui.html