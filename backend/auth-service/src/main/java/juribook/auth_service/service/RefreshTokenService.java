package juribook.auth_service.service;

import jakarta.servlet.http.HttpServletRequest;
import juribook.auth_service.config.JwtProperties;
import juribook.auth_service.model.entity.RefreshToken;
import juribook.auth_service.model.entity.User;
import juribook.auth_service.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final JwtProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Génère un nouveau refresh token aléatoire et le persiste (hashé).
     * Retourne le token en clair pour le poser en cookie (jamais re-récupérable).
     */
    @Transactional
    public String createForUser(User user, HttpServletRequest request) {
        String rawToken = generateRandomToken();
        String hash = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .tokenHash(hash)
            .expiresAt(OffsetDateTime.now().plusNanos(properties.refreshExpirationMs() * 1_000_000))
            .ipAddress(extractIp(request))
            .userAgent(extractUserAgent(request))
            .build();

        repository.save(refreshToken);
        log.debug("Refresh token créé pour user {} (expire: {})", user.getEmail(), refreshToken.getExpiresAt());
        return rawToken;
    }

    /**
     * Valide un refresh token reçu en cookie et procède à la rotation :
     * - Révoque l'ancien
     * - Crée un nouveau et le retourne
     * Lance IllegalStateException si le token est invalide, révoqué ou expiré.
     */
    @Transactional
    public RotationResult rotate(String rawToken, HttpServletRequest request) {
        String hash = hashToken(rawToken);
        RefreshToken existing = repository.findByTokenHash(hash)
            .orElseThrow(() -> new IllegalStateException("Refresh token inconnu"));

        if (!existing.isUsable()) {
            log.warn("Tentative d'utilisation d'un refresh token révoqué ou expiré (user: {})",
                     existing.getUser().getEmail());
            // Sprint 5+ : ici on peut détecter un vol et révoquer toute la chaîne
            throw new IllegalStateException("Refresh token invalide");
        }

        // Rotation : on crée le nouveau d'abord, puis on révoque l'ancien
        String newRawToken = generateRandomToken();
        String newHash = hashToken(newRawToken);

        RefreshToken newToken = RefreshToken.builder()
            .user(existing.getUser())
            .tokenHash(newHash)
            .expiresAt(OffsetDateTime.now().plusNanos(properties.refreshExpirationMs() * 1_000_000))
            .ipAddress(extractIp(request))
            .userAgent(extractUserAgent(request))
            .build();
        repository.save(newToken);

        existing.setRevoked(true);
        existing.setRevokedAt(OffsetDateTime.now());
        existing.setReplacedBy(newToken.getId());
        repository.save(existing);

        log.info("Refresh token rotation pour user: {}", existing.getUser().getEmail());

        return new RotationResult(existing.getUser(), newRawToken);
    }

    /** Révoque tous les refresh tokens actifs d'un user (logout). */
    @Transactional
    public void revokeAllForUser(UUID userId) {
        repository.revokeAllByUserId(userId, OffsetDateTime.now());
        log.info("Tous les refresh tokens révoqués pour user: {}", userId);
    }

    public record RotationResult(User user, String newRawToken) {}

    // ─── Helpers internes ────────────────────────────────────────────────

    private String generateRandomToken() {
        byte[] bytes = new byte[48]; // 384 bits — largement suffisant
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }

    private String extractIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua != null && ua.length() > 500) return ua.substring(0, 500);
        return ua;
    }
}