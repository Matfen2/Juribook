package juribook.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO d'entrée pour l'inscription standard (POST /api/auth/register).
 *
 * @Data (Lombok) génère getters/setters nécessaires à la désérialisation JSON
 * (Jackson) et à la validation (@Valid dans AuthController).
 *
 * IMPORTANT : pas de champ "role" ici. L'utilisateur ne choisit jamais son
 * rôle ; AuthService.create() assigne automatiquement Role.CLIENT.
 * Permettre au client d'envoyer "role": "ADMIN" serait une faille de
 * sécurité (élévation de privilèges à l'inscription).
 */
@Data
public class CreateUserRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    // Mot de passe en CLAIR à ce stade : AuthService le hache (BCrypt)
    // avant de l'assigner à l'entité User. Jamais stocké tel quel.
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String name;

    // Optionnel : aucune contrainte @NotBlank, l'utilisateur peut ne pas
    // renseigner de numéro de téléphone à l'inscription.
    private String phoneNumber;
}