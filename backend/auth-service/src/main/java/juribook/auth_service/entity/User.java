package juribook.auth_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité JPA représentant un utilisateur de la plateforme JuriBook
 * (table "users", créée par V1__create_users_table.sql).
 *
 * Un même schéma sert aux trois types d'utilisateurs (CLIENT, LAWYER, ADMIN),
 * distingués par le champ "role". Les informations métier propres aux avocats
 * (n° de barreau, spécialité, ville) ne sont PAS stockées ici : elles vivent
 * dans lawyer-service (table "lawyers"), reliées via un user_id.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor // Constructeur sans argument : indispensable pour Hibernate (instanciation par réflexion)
@AllArgsConstructor // Constructeur avec tous les champs : pratique pour les tests
@Builder // Permet User.builder()....build(), utilisé notamment dans AuthService.registerLawyer()
public class User {
    // Identifiant technique unique, généré automatiquement par PostgreSQL (BIGSERIAL)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Email = identifiant fonctionnel de connexion. Doit être unique en base
    // (contrainte UNIQUE côté SQL + vérification applicative via existsByEmail)
    @Column(nullable = false, unique = true)
    private String email;

    // Mot de passe HACHÉ (BCrypt) : jamais stocké ni manipulé en clair.
    // Le hachage est effectué dans AuthService avant la sauvegarde.
    @Column(name = "password", nullable = false)
    private String password;

    private String name;

    // Numéro de téléphone optionnel (colonne nullable)
    @Column(name = "phone_number")
    private String phoneNumber;

    // Rôle de l'utilisateur : CLIENT, LAWYER ou ADMIN.
    // IMPORTANT : ce champ n'est JAMAIS fourni par le client via les DTOs
    // d'inscription (CreateUserRequest / CreateLawyerRequest).
    // Puisque l'utilisateur n'a pas la possibilité de choisir son role.
    // Il est assigné automatiquement côté serveur par AuthService :
    //   - register        -> Role.CLIENT
    //   - register/lawyer -> Role.LAWYER
    // Stocké en base sous forme de texte ("CLIENT", "LAWYER", "ADMIN")
    // grâce à @Enumerated(EnumType.STRING), plus lisible/sûr que ORDINAL.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}