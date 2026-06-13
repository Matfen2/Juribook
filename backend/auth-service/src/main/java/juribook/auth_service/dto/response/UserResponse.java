package juribook.auth_service.dto.response;

import juribook.auth_service.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de réponse : ne contient JAMAIS le mot de passe,
 * même haché, pour ne pas l'exposer dans les réponses API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private Role role;
}