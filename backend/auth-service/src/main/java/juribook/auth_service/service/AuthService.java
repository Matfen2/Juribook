package juribook.auth_service.service;

import juribook.auth_service.dto.request.CreateLawyerRequest;
import juribook.auth_service.dto.request.CreateUserRequest;
import juribook.auth_service.dto.response.RegisterLawyerResponse;
import juribook.auth_service.dto.response.UserResponse;
import juribook.auth_service.entity.Role;
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

/**
 * Service métier pour la gestion des comptes utilisateurs (auth-service).
 *
 * @Transactional(readOnly = true) au niveau classe : par défaut, les méthodes
 * de lecture (getById, getAll) ouvrent une transaction en lecture seule
 * (optimisation Hibernate). Les méthodes qui écrivent (create, registerLawyer)
 * surchargent ce comportement avec leur propre @Transactional.
 *
 * @RequiredArgsConstructor (Lombok) génère un constructeur avec tous les champs
 * "final" ci-dessous : Spring injecte automatiquement userRepository,
 * userMapper et passwordEncoder via ce constructeur.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // Bean BCryptPasswordEncoder déclaré dans SecurityConfig,
    // utilisé pour hacher les mots de passe avant stockage en base.
    private final PasswordEncoder passwordEncoder;

    /**
     * Création d'un nouveau compte utilisateur (rôle CLIENT par défaut).
     *
     * POST /api/auth/register
     *
     * Étapes :
     * 1. Vérifie que l'email n'est pas déjà utilisé (sinon -> 409 Conflict
     *    via EmailAlreadyExistsException, gérée par GlobalExceptionHandler).
     * 2. Mappe le DTO vers l'entité (sans password/id/role, cf. UserMapper).
     * 3. Hache le mot de passe en clair reçu dans le DTO.
     * 4. Assigne le rôle CLIENT : l'utilisateur ne choisit jamais son rôle.
     * 5. Sauvegarde en base et renvoie la réponse (sans le mot de passe).
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
        user.setRole(Role.CLIENT);

        User saved = userRepository.save(user);

        log.info("Utilisateur créé avec succès : id={}, role={}", saved.getId(), saved.getRole());
        return userMapper.toResponse(saved);
    }

    /**
     * Inscription d'un avocat (rôle LAWYER, statut "en attente de validation").
     *
     * POST /api/auth/register/lawyer
     *
     * Étapes :
     * 1. Vérifie que l'email n'est pas déjà utilisé.
     * 2. Crée directement l'entité User via le Builder (pas de UserMapper ici :
     *    CreateLawyerRequest a une forme différente de CreateUserRequest).
     * 3. Hache le mot de passe et assigne le rôle LAWYER.
     * 4. Sauvegarde le compte dans authdb.users.
     * 5. Renvoie 201 avec le statut "PENDING_VALIDATION" (critère d'acceptation 1.3).
     *
     * NOTE : la création du profil avocat dans lawyer-service (table "lawyers",
     * avec barNumber/speciality/city) sera faite via un événement Kafka
     * "lawyer-registration-events", à implémenter dans une étape ultérieure.
     * Pour l'instant, seul le compte User (role=LAWYER) est créé ici ;
     * aucun profil Lawyer n'existe encore côté lawyer-service.
     */
    @Transactional
    public RegisterLawyerResponse registerLawyer(CreateLawyerRequest request) {
        log.info("Inscription d'un nouvel avocat : {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "L'email " + request.getEmail() + " est déjà utilisé");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .role(Role.LAWYER)
                .build();

        User saved = userRepository.save(user);
        log.info("Compte avocat créé : id={}, role={}", saved.getId(), saved.getRole());

        return RegisterLawyerResponse.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .name(saved.getName())
                .status("PENDING_VALIDATION")
                .build();
    }

    /**
     * Récupère un utilisateur par son identifiant technique.
     *
     * GET /api/auth/{id}
     *
     * Lève UserNotFoundException si aucun utilisateur ne correspond,
     * traduite en réponse 404 par GlobalExceptionHandler.
     */
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "Aucun utilisateur trouvé avec l'id " + id));

        return userMapper.toResponse(user);
    }

    /**
     * Récupère la liste des utilisateurs.
     *
     * GET /api/auth                -> tous les utilisateurs (findAll)
     * GET /api/auth?name=Jean      -> filtre par nom, recherche insensible
     *                                  à la casse et partielle
     *                                  (findByNameContainingIgnoreCase, cf. UserRepository)
     *
     * Chaque entité User est convertie en UserResponse via le mapper
     * (le mot de passe haché n'est jamais exposé).
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