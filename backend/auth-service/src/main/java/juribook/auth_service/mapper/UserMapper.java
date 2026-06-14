package juribook.auth_service.mapper;

import juribook.auth_service.dto.request.CreateUserRequest;
import juribook.auth_service.dto.response.UserResponse;
import juribook.auth_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct : convertit CreateUserRequest <-> User <-> UserResponse.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    // Entité -> Réponse API (jamais le mot de passe)
    UserResponse toResponse(User entity);

    // Requête -> Entité
    // - "password" est ignoré ici : haché (BCrypt) dans AuthService avant assignation
    // - "id" est ignoré : généré par la base de données
    // - "role" est ignoré : CreateUserRequest n'a pas ce champ,
    //   AuthService.create() assigne Role.CLIENT après le mapping
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toEntity(CreateUserRequest request);
}