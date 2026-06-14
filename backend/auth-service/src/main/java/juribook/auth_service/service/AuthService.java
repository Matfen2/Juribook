package juribook.auth_service.service;

import juribook.auth_service.dto.request.CreateUserRequest;
import juribook.auth_service.dto.request.LoginRequest;
import juribook.auth_service.dto.response.LoginResponse;
import juribook.auth_service.dto.response.UserResponse;
import juribook.auth_service.entity.Role;
import juribook.auth_service.entity.User;
import juribook.auth_service.exception.EmailAlreadyExistsException;
import juribook.auth_service.exception.InvalidCredentialsException;
import juribook.auth_service.exception.UserNotFoundException;
import juribook.auth_service.mapper.UserMapper;
import juribook.auth_service.repository.UserRepository;
import juribook.auth_service.security.JwtService;
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
    private final JwtService jwtService;
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
        user.setRole(Role.CLIENT); // rôle assigné côté serveur, jamais par le client

        User saved = userRepository.save(user);

        log.info("Utilisateur créé avec succès : id={}, role={}", saved.getId(), saved.getRole());
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

    /**
     * Authentifie un utilisateur et génère un token JWT.
     *
     * POST /api/auth/login
     *
     * Étapes :
     * 1. Recherche l'utilisateur par email.
     * 2. Vérifie le mot de passe avec passwordEncoder.matches() (compare
     *    le mot de passe en clair reçu avec le hash BCrypt stocké).
     * 3. En cas d'échec (email inconnu OU mot de passe incorrect), lève
     *    la MÊME exception avec le MÊME message dans les deux cas, pour
     *    ne pas indiquer à un attaquant si l'email existe.
     * 4. Génère le token JWT (subject=email, claims userId+role).
     * 5. Renvoie le token + ses métadonnées + le profil utilisateur.
     */
    public LoginResponse login(LoginRequest request) {
        log.info("Tentative de connexion : {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Email ou mot de passe incorrect");
        }

        String token = jwtService.generateToken(user);
        log.info("Connexion réussie : id={}, role={}", user.getId(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .user(userMapper.toResponse(user))
                .build();
    }
}