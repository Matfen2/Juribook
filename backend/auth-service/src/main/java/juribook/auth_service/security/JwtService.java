package juribook.auth_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import juribook.auth_service.entity.Role;
import juribook.auth_service.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Service de génération et validation des tokens JWT (Sprint 1.4).
 *
 * Algorithme : HMAC-SHA256 (HS256), signature symétrique avec une clé
 * secrète partagée (jwt.secret dans application.yaml).
 *
 * Contenu du token (claims) :
 * - subject : email de l'utilisateur (identifiant de connexion)
 * - userId  : id technique (utile pour retrouver l'utilisateur sans
 *             refaire de requête sur l'email)
 * - role    : CLIENT / LAWYER / ADMIN -> utilisé par le futur filtre JWT
 *             (Sprint 1.5) pour la protection des routes par rôle.
 *
 * La validation (isTokenValid, extractEmail, extractRole) n'est pas encore
 * utilisée à ce stade (1.4 ne couvre que la génération), mais sera le coeur
 * du filtre JwtAuthFilter du Sprint 1.5.
 */
@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    /**
     * Construit la clé de signature à partir du secret configuré.
     * Keys.hmacShaKeyFor exige une clé d'au moins 256 bits (32 octets)
     * pour HS256 - le secret par défaut ci-dessus en fait 64.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Génère un token JWT signé pour l'utilisateur donné.
     *
     * POST /api/auth/login appelle cette méthode après vérification
     * du mot de passe (cf. AuthService.login()).
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrait l'email (subject) d'un token. Lève JwtException si le token
     * est invalide ou expiré (utilisé par le futur filtre Sprint 1.5).
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extrait le rôle porté par le token, utilisé par Spring Security
     * pour autoriser/refuser l'accès à une route (Sprint 1.5).
     */
    public Role extractRole(String token) {
        return Role.valueOf(parseClaims(token).get("role", String.class));
    }

    public Long extractUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    /**
     * Vérifie que le token est valide : signature correcte ET non expiré.
     * Renvoie false (plutôt que de propager l'exception) pour simplifier
     * son usage dans un filtre de sécurité.
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parse et vérifie la signature du token, renvoie ses claims.
     * Lève JwtException (ExpiredJwtException, SignatureException, ...)
     * si le token est invalide.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Durée de validité configurée, en secondes (utilisée dans
     * LoginResponse.expiresIn pour informer le client).
     */
    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }
}