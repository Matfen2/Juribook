package juribook.auth_service.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message); // message récupérable depuis GlobalExceptionHandler.java
    }
}