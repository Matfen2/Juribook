package juribook.auth_service.service;

import juribook.auth_service.dto.RegisterClientRequest;
import juribook.auth_service.dto.RegisterLawyerRequest;
import juribook.auth_service.dto.RegisterLawyerResponse;
import juribook.auth_service.dto.RegisterResponse;
import juribook.auth_service.exception.BarNumberAlreadyExistsException;
import juribook.auth_service.exception.EmailAlreadyExistsException;
import juribook.auth_service.exception.RoleNotFoundException;
import juribook.auth_service.exception.SpecialtyNotFoundException;
import juribook.auth_service.model.entity.Lawyer;
import juribook.auth_service.model.entity.Role;
import juribook.auth_service.model.entity.Specialty;
import juribook.auth_service.model.entity.User;
import juribook.auth_service.repository.LawyerRepository;
import juribook.auth_service.repository.RoleRepository;
import juribook.auth_service.repository.SpecialtyRepository;
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
    private final SpecialtyRepository specialtyRepository;  
    private final LawyerRepository lawyerRepository; 

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

    @Transactional
    public RegisterLawyerResponse registerLawyer(RegisterLawyerRequest request) {
        // 1. Vérifier l'unicité de l'email et du numéro de barreau
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Tentative d'inscription avocat avec email existant: {}", request.email());
            throw new EmailAlreadyExistsException(request.email());
        }
        if (lawyerRepository.existsByBarNumber(request.barNumber())) {
            log.warn("Tentative d'inscription avec n° de barreau existant: {}", request.barNumber());
            throw new BarNumberAlreadyExistsException(request.barNumber());
        }

        // 2. Récupérer le rôle LAWYER et la spécialité
        Role lawyerRole = roleRepository.findByName(Role.RoleName.LAWYER)
            .orElseThrow(() -> new RoleNotFoundException(Role.RoleName.LAWYER));
        Specialty specialty = specialtyRepository.findByCode(request.specialtyCode())
            .orElseThrow(() -> new SpecialtyNotFoundException(request.specialtyCode()));

        // 3. Hasher le mot de passe
        String passwordHash = passwordEncoder.encode(request.password());

        // 4. Créer le User avec enabled=false (statut "en attente de validation")
        User user = User.builder()
            .email(request.email())
            .passwordHash(passwordHash)
            .firstName(request.firstName())
            .lastName(request.lastName())
            .phone(request.phone())
            .enabled(false)              // ← critère "en attente de validation"
            .roles(Set.of(lawyerRole))
            .build();
        User savedUser = userRepository.save(user);

        // 5. Créer le profil Lawyer associé
        Lawyer lawyer = Lawyer.builder()
            .user(savedUser)             // @MapsId mappe userId depuis user.id
            .barNumber(request.barNumber())
            .specialty(specialty)
            .city(request.city())
            .build();
        Lawyer savedLawyer = lawyerRepository.save(lawyer);

        log.info("Nouvel avocat inscrit (en attente de validation): id={}, email={}, bar={}",
                savedUser.getId(), savedUser.getEmail(), savedLawyer.getBarNumber());

        // 6. Mapper vers DTO de réponse
        return new RegisterLawyerResponse(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getFirstName(),
            savedUser.getLastName(),
            savedLawyer.getBarNumber(),
            specialty.getName(),
            savedLawyer.getCity(),
            "PENDING_VALIDATION",
            savedUser.getCreatedAt()
        );
    }
}