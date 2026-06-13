package juribook.auth_service.exception;

public class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String id) {
            super("Utilisateur introuvable : " + id); // message récupérable depuis GlobalExceptionHandler.java
        }
}
