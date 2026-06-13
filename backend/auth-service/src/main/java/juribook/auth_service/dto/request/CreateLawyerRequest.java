package juribook.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO d'entrée pour l'inscription d'un avocat (POST /api/auth/register/lawyer).
 *
 * Contient à la fois :
 * - les champs nécessaires à la création du compte User (email, password, name, phoneNumber),
 * - les champs métier propres à l'avocat (barNumber, speciality, city), transmis ensuite
 *   à lawyer-service via l'événement Kafka LawyerRegistrationRequestedEvent.
 *
 * Pas de champ "role" : assigné automatiquement à Role.LAWYER par AuthService.registerLawyer().
 */
@Data
public class CreateLawyerRequest {

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

    // ─── Champs spécifiques avocat ──────────────────────────
    @NotNull(message = "Le numéro de barreau est obligatoire")
    private Long barNumber;

    // Validation d'entrée dupliquée depuis lawyer-service.Speciality :
    // évite de publier un événement Kafka avec une valeur invalide
    // qui échouerait silencieusement côté consumer (Speciality.valueOf()).
    @NotBlank(message = "La spécialité est obligatoire")
    @Pattern(
        regexp = "DROIT_DU_TRAVAIL|DROIT_DE_LA_DEFENSE|DROIT_DE_LA_FAMILLE|DROIT_IMMOBILIER|" +
                 "DROIT_DES_AFFAIRES|DROIT_FISCAL|DROIT_DE_LA_CONSOMMATION|DROIT_DES_ETRANGERS|" +
                 "DROIT_DE_LA_SANTE|DROIT_DES_ASSURANCES",
        message = "Spécialité invalide"
    )
    private String speciality;

    @NotBlank(message = "La ville est obligatoire")
    private String city;
}