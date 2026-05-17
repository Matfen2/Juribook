package juribook.auth_service.exception;

public class BarNumberAlreadyExistsException extends RuntimeException {
    public BarNumberAlreadyExistsException(String barNumber) {
        super("Un avocat est déjà inscrit avec le numéro de barreau: " + barNumber);
    }
}