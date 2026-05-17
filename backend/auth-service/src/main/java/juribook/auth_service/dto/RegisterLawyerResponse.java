package juribook.auth_service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RegisterLawyerResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    String barNumber,
    String specialty,
    String city,
    String status,           // "PENDING_VALIDATION"
    OffsetDateTime createdAt
) {}