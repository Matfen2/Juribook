package juribook.auth_service.exception;

/**
 * Exception levée lorsque l'email n'existe pas OU que le mot de passe
 * ne correspond pas (cf. AuthService.login()).
 *
 * Le même message est utilisé dans les deux cas (cf. AuthService) afin
 * de ne pas révéler si un email est enregistré ou non (énumération
 * d'utilisateurs = faille de sécurité classique).
 *
 * Transformée en réponse HTTP 401 par GlobalExceptionHandler.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}