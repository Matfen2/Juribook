package juribook.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "timestamp", OffsetDateTime.now(),
            "status",    409,
            "error",     "Conflict",
            "message",   ex.getMessage()
        ));
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRoleNotFound(RoleNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "timestamp", OffsetDateTime.now(),
            "status",    500,
            "error",     "Internal Server Error",
            "message",   ex.getMessage()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
            errors.put(err.getField(), err.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "timestamp", OffsetDateTime.now(),
            "status",    400,
            "error",     "Bad Request",
            "message",   "Erreur de validation",
            "fields",    errors
        ));
    }

    @ExceptionHandler(SpecialtyNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleSpecialtyNotFound(SpecialtyNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "timestamp", OffsetDateTime.now(),
            "status",    400,
            "error",     "Bad Request",
            "message",   ex.getMessage()
        ));
    }

    @ExceptionHandler(BarNumberAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleBarNumberExists(BarNumberAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "timestamp", OffsetDateTime.now(),
            "status",    409,
            "error",     "Conflict",
            "message",   ex.getMessage()
        ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "timestamp", OffsetDateTime.now(),
            "status",    401,
            "error",     "Unauthorized",
            "message",   "Email ou mot de passe incorrect"
        ));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabled(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "timestamp", OffsetDateTime.now(),
            "status",    401,
            "error",     "Unauthorized",
            "message",   "Compte en attente de validation par un administrateur"
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "timestamp", OffsetDateTime.now(),
            "status",    403,
            "error",     "Forbidden",
            "message",   "Vous n'avez pas les permissions nécessaires pour accéder à cette ressource"
        ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationFailure(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "timestamp", OffsetDateTime.now(),
            "status",    401,
            "error",     "Unauthorized",
            "message",   "Authentification requise"
        ));
    }
}