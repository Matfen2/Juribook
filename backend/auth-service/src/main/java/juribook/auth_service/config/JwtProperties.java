package juribook.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "juribook.jwt")
public record JwtProperties(
    String secret,
    long expirationMs,
    String cookieName
) {}