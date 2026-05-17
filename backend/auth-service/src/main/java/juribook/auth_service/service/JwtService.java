package juribook.auth_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import juribook.auth_service.config.JwtProperties;
import juribook.auth_service.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtProperties properties;

    /** Génère un nouveau token JWT pour un user authentifié. */
    public String generateToken(User user) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + properties.expirationMs());

        List<String> roles = user.getRoles().stream()
            .map(role -> role.getName().name())
            .toList();

        return Jwts.builder()
            .subject(user.getId().toString())
            .claim("email", user.getEmail())
            .claim("roles", roles)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey())
            .compact();
    }

    /** Extrait l'UUID du user depuis le token (claim "sub"). */
    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaim(token, Claims::getSubject));
    }

    /** Extrait l'email depuis le token. */
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    /** Vérifie que le token est valide (signature correcte + non expiré). */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);   // déclenche une exception si invalide/expiré
            return true;
        } catch (Exception e) {
            log.debug("Token JWT invalide: {}", e.getMessage());
            return false;
        }
    }

    // ─── Helpers internes ─────────────────────────────────────────────

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey signingKey() {
        byte[] keyBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}