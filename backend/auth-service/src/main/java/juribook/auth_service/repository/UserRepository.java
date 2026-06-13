package juribook.auth_service.repository;

import juribook.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Recherche un utilisateur par email (il faudra le sécuriser plus tard via JWT)
    Optional<User> findByEmail(String email);

    // Vérifie si un email est déjà utilisé (utile pour l'inscription)
    boolean existsByEmail(String email);

    // Recherche insensible à la casse, pour le filtre GET /api/auth/users?name=...
    List<User> findByNameContainingIgnoreCase(String name);
}