# lawyer-service

Profils publics des avocats, recherche et photos de JuriBook.

| | |
|---|---|
| **Port** | `8082` |
| **Base** | PostgreSQL - `lawyerdb` (user/mdp : `juribook` / `juribook`) |
| **Stockage** | MinIO (S3-compatible) - photos de profil |
| **Stack** | Java 21 · Spring Boot 4 · Spring Security · JWT (JJWT 0.13) · Spring Data JPA · Flyway · MinIO · Docker |

## Lancer en local

```bash
mvn spring-boot:run
```

Swagger : http://localhost:8082/swagger-ui.html