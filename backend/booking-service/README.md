# booking-service

Disponibilités et créneaux de rendez-vous de JuriBook.

| | |
|---|---|
| **Port** | `8083` |
| **Base** | PostgreSQL - `bookingdb` (user/mdp : `juribook` / `juribook`) |
| **Stack** | Java 21 · Spring Boot 4 · Spring Data JPA · Flyway · Docker |

## Lancer en local

```bash
mvn spring-boot:run
```

Swagger : http://localhost:8083/swagger-ui.html