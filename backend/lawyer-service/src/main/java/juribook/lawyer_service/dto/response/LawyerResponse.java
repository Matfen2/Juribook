package juribook.lawyer_service.dto.response;

import juribook.lawyer_service.entity.LawyerStatus;
import juribook.lawyer_service.entity.Speciality;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de réponse renvoyé par LawyerController (GET /api/lawyers, GET /api/lawyers/{id}).
 *
 * Reflète directement les champs de l'entité Lawyer. Ne contient aucune
 * information d'identité (email, nom...) : pour afficher le nom de l'avocat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LawyerResponse {
    private Long id;
    private Long userId;
    private Long barNumber;
    private Speciality speciality;
    private String city;
    private LawyerStatus status;
}