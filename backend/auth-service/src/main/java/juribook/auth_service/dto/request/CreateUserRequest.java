package juribook.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO d'entrée pour l'inscription standard (POST /api/auth/register).
 *
 * Pas de champ "role" : l'utilisateur ne choisit jamais son rôle.
 * AuthService.create() assigne automatiquement Role.CLIENT.
 * Permettre au client d'envoyer "role": "ADMIN" serait une faille
 * d'élévation de privilèges à l'inscription.
 */
@Data
public class CreateUserRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String name;

    private String phoneNumber;
}