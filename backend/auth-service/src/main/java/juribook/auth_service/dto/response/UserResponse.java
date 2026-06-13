package juribook.auth_service.dto.response;

import juribook.auth_service.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de réponse renvoyé par AuthController (register, getById, getAll).
 *
 * Règle de sécurité : ne contient JAMAIS le mot de passe, même haché.
 * Un hash BCrypt n'a aucune raison d'être exposé dans une réponse JSON,
 * même s'il n'est techniquement pas "déchiffrable".
 *
 * @Data (Lombok) génère getters/setters/equals/hashCode/toString.
 * @Builder permet UserResponse.builder()....build(), utilisé par UserMapper
 * (généré automatiquement par MapStruct) et par AuthService.registerLawyer().
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    // Identifiant technique de l'utilisateur (généré en base)
    private Long id;

    private String email;

    private String name;

    // Optionnel : peut être null si l'utilisateur n'a pas renseigné de téléphone
    private String phoneNumber;

    // Rôle de l'utilisateur (CLIENT, LAWYER, ADMIN).
    // Exposé en lecture uniquement : utile pour que le frontend sache
    // quelles pages/actions afficher selon le rôle (ex: redirection après login).
    private Role role;
}