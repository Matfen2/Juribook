package juribook.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de réponse pour l'inscription d'un avocat (POST /api/auth/register/lawyer).
 *
 * Différent de LawyerResponse (lawyer-service) : ce DTO confirme la création
 * du compte utilisateur (role=LAWYER) côté auth-service, avec le statut
 * "PENDING_VALIDATION" indiquant que le profil avocat (table lawyers,
 * lawyer-service) reste à créer/valider.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterLawyerResponse {
    private Long id;
    private String email;
    private String name;
    private String status; // "PENDING_VALIDATION"
}