package juribook.auth_service.mapper;

import juribook.auth_service.dto.request.CreateUserRequest;
import juribook.auth_service.dto.response.UserResponse;
import juribook.auth_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Entité -> Réponse API (jamais le mot de passe)
    UserResponse toResponse(User entity);

    // Requête -> Entité
    // - "password" est ignoré ici : il sera haché (BCrypt) dans le service avant d'être assigné
    // - "role" est converti depuis la String du DTO vers l'enum Role
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", expression = "java(Role.valueOf(request.getRole().toUpperCase()))")
    User toEntity(CreateUserRequest request);
}