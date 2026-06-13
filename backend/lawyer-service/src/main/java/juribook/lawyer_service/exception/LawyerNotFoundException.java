package juribook.lawyer_service.exception;

/**
 * Exception levée lorsqu'aucun profil avocat ne correspond à l'identifiant
 * demandé (cf. LawyerService.getById).
 *
 * Transformée en réponse HTTP 404 par GlobalExceptionHandler.
 */
public class LawyerNotFoundException extends RuntimeException {
    public LawyerNotFoundException(String message) {
        super(message);
    }
}