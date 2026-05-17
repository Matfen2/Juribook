# JuriBook - Plateforme de prise de rendez-vous pour avocats

> "Le Doctolib des avocats" - Permettre aux particuliers de rechercher un avocat par spécialité, consulter ses disponibilités et réserver un rendez-vous en ligne.

---

## Stack technique

| Couche               | Technologie                                                  |
| -------------------- | ------------------------------------------------------------ |
| Backend              | Spring Boot 4.0.6 · Java 21 · Maven                          |
| Frontend             | React 19 · TypeScript · Tailwind CSS v4 · Vite 6             |
| Base de données      | PostgreSQL 16 · Flyway · Hibernate 7                         |
| Messaging            | Apache Kafka 3.9 (mode KRaft, sans Zookeeper) · Apicurio Schema Registry 3.0.5 |
| Auth (Sprint 1+)     | Spring Security 7 · JWT                                       |
| API Docs             | SpringDoc OpenAPI / Swagger UI 3.0.3                         |
| Conteneurisation     | Docker · Docker Compose                                       |
| CI/CD                | GitHub Actions                                                |

---

## Architecture monorepo

```
juribook/
├── backend/
│   ├── auth-service/          → port 8081 (JWT, register, login)
│   ├── lawyer-service/        → port 8082 (profils avocats)
│   ├── booking-service/       → port 8083 (réservations)
│   ├── notification-service/  → port 8084 (emails via Kafka)
│   └── audit-service/         → port 8085 (logs métier via Kafka)
├── frontend/                  → port 5173 (React + Vite)
├── docker/
│   └── docker-compose.yml
├── .github/
│   └── workflows/
│       ├── backend-ci.yml
│       └── frontend-ci.yml
├── .gitattributes
├── .gitignore
└── README.md
```