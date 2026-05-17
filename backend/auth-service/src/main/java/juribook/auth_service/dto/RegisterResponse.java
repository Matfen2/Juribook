package juribook.auth_service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RegisterResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    OffsetDateTime createdAt
) {}