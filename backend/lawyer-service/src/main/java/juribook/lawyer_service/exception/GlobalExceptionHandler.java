package juribook.lawyer_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global des exceptions de lawyer-service.
 *
 * @RestControllerAdvice : intercepte les exceptions levées par n'importe
 * quel @RestController de ce service et les transforme en réponse JSON
 * standardisée (timestamp, status, error, message).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── Avocat non trouvé (LawyerNotFoundException) ──────────
    @ExceptionHandler(LawyerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleLawyerNotFound(LawyerNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ─── Méthode utilitaire commune ────────────────────────────
    // Construit une réponse d'erreur JSON cohérente, réutilisée par
    // tous les @ExceptionHandler de cette classe.
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }
}