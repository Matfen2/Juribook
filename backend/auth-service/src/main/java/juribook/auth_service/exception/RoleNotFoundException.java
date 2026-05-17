package juribook.auth_service.exception;

import juribook.auth_service.model.entity.Role;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(Role.RoleName name) {
        super("Le rôle système est introuvable: " + name +
              " (vérifier que la migration Flyway V1 s'est bien exécutée)");
    }
}