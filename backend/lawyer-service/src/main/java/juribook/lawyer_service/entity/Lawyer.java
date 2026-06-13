package juribook.lawyer_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité JPA représentant le profil métier d'un avocat
 * (table "lawyers", créée par V1__create_lawyers_table.sql, base lawyerdb).
 *
 * Volontairement minimaliste : ne duplique PAS email/password/name/phoneNumber/role,
 * qui restent la responsabilité de auth-service (table "users", base authdb).
 * Le lien entre les deux services se fait via "userId" (pas de clé étrangère SQL :
 * bases de données séparées entre microservices).
 *
 * Créé automatiquement par LawyerRegistrationConsumer lors de la réception
 * de l'événement Kafka "lawyer-registration-events" (publié par auth-service
 * lors de POST /api/auth/register/lawyer).
 */
@Entity
@Table(name = "lawyers")
@Getter
@Setter
@NoArgsConstructor // Indispensable pour Hibernate (instanciation par réflexion)
@AllArgsConstructor
@Builder // Utilisé par LawyerRegistrationConsumer pour construire l'entité
public class Lawyer {

    // Identifiant technique propre à lawyer-service (différent de userId)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Référence vers l'id de l'utilisateur dans auth-service.users.
    // UNIQUE : un utilisateur ne peut avoir qu'un seul profil avocat.
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // Numéro de barreau : UNIQUE également (deux avocats ne peuvent pas
    // partager le même numéro).
    @Column(name = "bar_number", nullable = false, unique = true)
    private Long barNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Speciality speciality;

    @Column(nullable = false)
    private String city;

    // Statut de validation, initialisé à PENDING_VALIDATION à la création
    // (LawyerRegistrationConsumer).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LawyerStatus status;
}