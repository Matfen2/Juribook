package juribook.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterLawyerRequest(

    // ─── Données utilisateur (identiques à RegisterClientRequest) ─────
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 254)
    String email,

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 100)
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
        message = "Le mot de passe doit contenir au moins une minuscule, une majuscule et un chiffre"
    )
    String password,

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    String firstName,

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    String lastName,

    @Pattern(
        regexp = "^(\\+33|0)[1-9](\\d{2}){4}$",
        message = "Numéro de téléphone français invalide"
    )
    String phone,

    // ─── Données spécifiques avocat ───────────────────────────────────
    @NotBlank(message = "Le numéro de barreau est obligatoire")
    @Size(min = 3, max = 50)
    String barNumber,

    @NotBlank(message = "La spécialité est obligatoire")
    String specialtyCode,    // ex: "DROIT_TRAVAIL"

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 100)
    String city
) {}