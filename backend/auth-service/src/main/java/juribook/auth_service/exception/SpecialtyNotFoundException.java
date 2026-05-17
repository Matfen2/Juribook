package juribook.auth_service.exception;

public class SpecialtyNotFoundException extends RuntimeException {
    public SpecialtyNotFoundException(String code) {
        super("Spécialité inconnue: " + code);
    }
}