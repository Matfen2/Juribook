package juribook.auth_service.service;

import juribook.auth_service.dto.RegisterClientRequest;
import juribook.auth_service.dto.RegisterResponse;
import juribook.auth_service.exception.EmailAlreadyExistsException;
import juribook.auth_service.exception.RoleNotFoundException;
import juribook.auth_service.model.entity.Role;
import juribook.auth_service.model.entity.User;
import juribook.auth_service.repository.RoleRepository;
import juribook.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse registerClient(RegisterClientRequest request) {
        // 1. Vérifier que l'email n'est pas déjà pris
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Tentative d'inscription avec email existant: {}", request.email());
            throw new EmailAlreadyExistsException(request.email());
        }

        // 2. Récupérer le rôle CLIENT depuis la DB
        Role clientRole = roleRepository.findByName(Role.RoleName.CLIENT)
            .orElseThrow(() -> new RoleNotFoundException(Role.RoleName.CLIENT));

        // 3. Hasher le mot de passe (BCrypt)
        String passwordHash = passwordEncoder.encode(request.password());

        // 4. Construire et persister l'utilisateur
        User user = User.builder()
            .email(request.email())
            .passwordHash(passwordHash)
            .firstName(request.firstName())
            .lastName(request.lastName())
            .phone(request.phone())
            .enabled(true)
            .roles(Set.of(clientRole))
            .build();

        User saved = userRepository.save(user);
        log.info("Nouvel utilisateur CLIENT inscrit: id={}, email={}", saved.getId(), saved.getEmail());

        // 5. Mapper vers DTO de réponse
        return new RegisterResponse(
            saved.getId(),
            saved.getEmail(),
            saved.getFirstName(),
            saved.getLastName(),
            saved.getCreatedAt()
        );
    }
}