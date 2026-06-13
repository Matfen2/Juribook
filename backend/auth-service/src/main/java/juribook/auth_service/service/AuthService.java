package juribook.auth_service.service;

import juribook.auth_service.dto.request.CreateUserRequest;
import juribook.auth_service.dto.response.UserResponse;
import juribook.auth_service.entity.User;
import juribook.auth_service.exception.EmailAlreadyExistsException;
import juribook.auth_service.exception.UserNotFoundException;
import juribook.auth_service.mapper.UserMapper;
import juribook.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Création d'un nouveau compte utilisateur.
     */
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        log.info("Création d'un nouvel utilisateur : {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "L'email " + request.getEmail() + " est déjà utilisé");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);

        log.info("Utilisateur créé avec succès : id={}", saved.getId());
        return userMapper.toResponse(saved);
    }

    /**
     * Récupère un utilisateur par son identifiant.
     * Lève UserNotFoundException si aucun utilisateur ne correspond.
     */
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "Aucun utilisateur trouvé avec l'id " + id));

        return userMapper.toResponse(user);
    }

    /**
     * Récupère tous les utilisateurs, ou filtre par nom si "name" est fourni.
     */
    public List<UserResponse> getAll(String name) {
        List<User> users = (name == null || name.isBlank())
                ? userRepository.findAll()
                : userRepository.findByNameContainingIgnoreCase(name);

        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }
}