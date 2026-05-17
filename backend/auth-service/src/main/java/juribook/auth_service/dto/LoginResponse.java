package juribook.auth_service.dto;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    List<String> roles    // ex: ["CLIENT"] ou ["LAWYER"]
) {}