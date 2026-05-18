package juribook.auth_service.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "juribook.outbox")
public record OutboxProperties(
    long pollIntervalMs,
    int batchSize,
    int maxRetries
) {}