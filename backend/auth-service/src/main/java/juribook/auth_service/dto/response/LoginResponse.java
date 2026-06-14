package juribook.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de réponse pour POST /api/auth/login.
 *
 * "tokenType" = "Bearer" : indique au client comment utiliser le token
 * dans les futures requêtes : header "Authorization: Bearer {token}".
 *
 * "expiresIn" : durée de validité en secondes, permet au frontend
 * d'anticiper le renouvellement (utile pour le refresh token, Sprint 1.7).
 *
 * "user" : informations de profil immédiatement utiles au frontend
 * (id, role...) pour la redirection post-login par rôle (Sprint 1.6).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private UserResponse user;
}