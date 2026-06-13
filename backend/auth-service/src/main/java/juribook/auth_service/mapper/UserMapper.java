package juribook.auth_service.mapper;

import juribook.auth_service.dto.request.CreateUserRequest;
import juribook.auth_service.dto.response.UserResponse;
import juribook.auth_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct : fait le pont entre les DTOs (entrée/sortie API)
 * et l'entité JPA User.
 *
 * MapStruct génère automatiquement l'implémentation (UserMapperImpl) à la
 * compilation, annotée @Component grâce à componentModel = "spring" —
 * elle est donc injectable directement dans AuthService.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Entité -> Réponse API (utilisée pour register, getById, getAll).
     *
     * "id", "email", "name", "phoneNumber", "role" sont mappés automatiquement
     * car les noms de champs correspondent exactement entre User et UserResponse.
     *
     * Le mot de passe (haché) n'est volontairement PAS présent dans UserResponse :
     * il ne doit jamais apparaître dans une réponse JSON, même haché.
     */
    UserResponse toResponse(User entity);

    /**
     * Requête (DTO d'inscription) -> Entité.
     *
     * Trois champs sont explicitement ignorés car ils n'existent pas dans
     * CreateUserRequest et doivent être renseignés après le mapping :
     *
     * - "password" : le mot de passe reçu est en CLAIR dans le DTO.
     *   AuthService le hache (BCrypt) puis appelle user.setPassword(...).
     *
     * - "id" : généré par la base de données (BIGSERIAL), jamais fourni en entrée.
     *
     * - "role" : l'utilisateur ne choisit JAMAIS son rôle. AuthService assigne
     *   Role.CLIENT (inscription standard) ou Role.LAWYER (inscription avocat)
     *   après avoir appelé toEntity(...).
     */
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toEntity(CreateUserRequest request);
}