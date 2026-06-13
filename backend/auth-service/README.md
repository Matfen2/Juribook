# auth-service

Authentification et gestion des comptes de JuriBook.

| | |
|---|---|
| **Port** | `8081` |
| **Base** | PostgreSQL - `authdb` (user/mdp : `juribook` / `juribook`) |
| **Stack** | Java 21 · Spring Boot 4 · Spring Security · JWT (JJWT 0.13) · Spring Data JPA · Flyway · Docker |

## Endpoints

| Méthode | Route                          | Accès        | Description                            |
|---------|--------------------------------|--------------|----------------------------------------|
| `POST`  | `/api/auth/register`           | public       | Inscription client                     |
| `POST`  | `/api/auth/register/lawyer`    | public       | Inscription avocat                     |
| `GET`   | `/api/auth/1`                  | public       | Récupération d'un utilisateur          |
| `GET`   | `/api/auth`                    | public       | Récupération de tous les utilisateurs  |

## Migration Flyway/Tableaux SQL
| Version | Contenu                                              |
|---------|------------------------------------------------------|
| `V1`    | Table `users`                                        |

## Lancer en local

```bash
mvn spring-boot:run
```

## Test

```bash
mvn test
```

## Vérification
Liste des utilisateurs (meme le mot de passe hashé)
```
docker exec -it juribook psql -U juribook -d authdb -c "SELECT * FROM users" 
```

ou 

```
docker exec -it juribook-postgres psql -U juribook -d authdb
```

### Documentation Swagger API
Swagger : http://localhost:8081/swagger-ui.html