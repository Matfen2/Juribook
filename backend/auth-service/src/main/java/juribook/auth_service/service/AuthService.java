package juribook.auth_service.service;

import jakarta.servlet.http.HttpServletRequest;
import juribook.auth_service.dto.LoginRequest;
import juribook.auth_service.dto.LoginResponse;
import juribook.auth_service.dto.RegisterClientRequest;
import juribook.auth_service.dto.RegisterLawyerRequest;
import juribook.auth_service.dto.RegisterLawyerResponse;
import juribook.auth_service.dto.RegisterResponse;
import juribook.auth_service.events.AvroSerializer;
import juribook.auth_service.exception.BarNumberAlreadyExistsException;
import juribook.auth_service.exception.EmailAlreadyExistsException;
import juribook.auth_service.exception.RoleNotFoundException;
import juribook.auth_service.exception.SpecialtyNotFoundException;
import juribook.auth_service.model.entity.Lawyer;
import juribook.auth_service.model.entity.Role;
import juribook.auth_service.model.entity.Specialty;
import juribook.auth_service.model.entity.User;
import juribook.auth_service.outbox.OutboxEvent;
import juribook.auth_service.outbox.OutboxRepository;
import juribook.auth_service.repository.LawyerRepository;
import juribook.auth_service.repository.RoleRepository;
import juribook.auth_service.repository.SpecialtyRepository;
import juribook.auth_service.repository.UserRepository;
import juribook.events.lawyer.LawyerRegisteredPayload;
import juribook.events.lawyer.LawyerRegisteredV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SpecialtyRepository specialtyRepository;
    private final LawyerRepository lawyerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final OutboxRepository outboxRepository;
    private final AvroSerializer avroSerializer;

    // ─── Inscription client (1.2) ───────────────────────────────────────
    @Transactional
    public RegisterResponse registerClient(RegisterClientRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Tentative d'inscription avec email existant: {}", request.email());
            throw new EmailAlreadyExistsException(request.email());
        }

        Role clientRole = roleRepository.findByName(Role.RoleName.CLIENT)
            .orElseThrow(() -> new RoleNotFoundException(Role.RoleName.CLIENT));

        String passwordHash = passwordEncoder.encode(request.password());

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

        return new RegisterResponse(
            saved.getId(),
            saved.getEmail(),
            saved.getFirstName(),
            saved.getLastName(),
            saved.getCreatedAt()
        );
    }

    // ─── Inscription avocat (1.3 + 2.2.B outbox) ────────────────────────
    @Transactional
    public RegisterLawyerResponse registerLawyer(RegisterLawyerRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Tentative d'inscription avocat avec email existant: {}", request.email());
            throw new EmailAlreadyExistsException(request.email());
        }
        if (lawyerRepository.existsByBarNumber(request.barNumber())) {
            log.warn("Tentative d'inscription avec n° de barreau existant: {}", request.barNumber());
            throw new BarNumberAlreadyExistsException(request.barNumber());
        }

        Role lawyerRole = roleRepository.findByName(Role.RoleName.LAWYER)
            .orElseThrow(() -> new RoleNotFoundException(Role.RoleName.LAWYER));
        Specialty specialty = specialtyRepository.findByCode(request.specialtyCode())
            .orElseThrow(() -> new SpecialtyNotFoundException(request.specialtyCode()));

        String passwordHash = passwordEncoder.encode(request.password());

        User user = User.builder()
            .email(request.email())
            .passwordHash(passwordHash)
            .firstName(request.firstName())
            .lastName(request.lastName())
            .phone(request.phone())
            .enabled(false)  // "en attente de validation"
            .roles(Set.of(lawyerRole))
            .build();
        User savedUser = userRepository.save(user);

        Lawyer lawyer = Lawyer.builder()
            .user(savedUser)
            .barNumber(request.barNumber())
            .specialty(specialty)
            .city(request.city())
            .build();
        Lawyer savedLawyer = lawyerRepository.save(lawyer);

        // ─── Outbox event (Sprint 2.2.B) ──────────────────────────────
        // Écrit dans la même transaction JPA que User + Lawyer.
        // Soit les 3 commitent ensemble, soit tout rollback : pas de dual-write.
        publishLawyerRegisteredEvent(savedUser, savedLawyer);

        log.info("Nouvel avocat inscrit (en attente de validation): id={}, email={}, bar={}",
                 savedUser.getId(), savedUser.getEmail(), savedLawyer.getBarNumber());

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

    // ─── Login (1.4 + 1.7 : émet access + refresh) ──────────────────────
    public LoginResult login(LoginRequest request, HttpServletRequest httpRequest) {
        // Spring vérifie email + password + enabled (lance BadCredentials/Disabled si invalide)
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new IllegalStateException("User disparu après auth — incohérent"));

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createForUser(user, httpRequest);

        List<String> roles = user.getRoles().stream()
            .map(r -> r.getName().name())
            .toList();

        LoginResponse response = new LoginResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            roles
        );

        log.info("Login réussi pour: {} (rôles: {})", user.getEmail(), roles);
        return new LoginResult(accessToken, refreshToken, response);
    }

    // ─── Refresh : rotation du refresh token (1.7) ──────────────────────
    public LoginResult refresh(String refreshTokenRaw, HttpServletRequest httpRequest) {
        RefreshTokenService.RotationResult rotation =
            refreshTokenService.rotate(refreshTokenRaw, httpRequest);

        User user = rotation.user();
        String newAccessToken = jwtService.generateToken(user);

        List<String> roles = user.getRoles().stream()
            .map(r -> r.getName().name())
            .toList();

        LoginResponse response = new LoginResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            roles
        );

        return new LoginResult(newAccessToken, rotation.newRawToken(), response);
    }

    // ─── Logout : révoque tous les refresh tokens du user (1.7) ────────
    public void logout(UUID userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    // ─── Publication de l'event lawyer.registered via outbox (2.2.B) ────
    private void publishLawyerRegisteredEvent(User user, Lawyer lawyer) {
        LawyerRegisteredPayload payload = LawyerRegisteredPayload.newBuilder()
            .setUserId(user.getId())
            .setEmail(user.getEmail())
            .setFirstName(user.getFirstName())
            .setLastName(user.getLastName())
            .setPhone(user.getPhone())  // peut être null, accepté par l'union Avro
            .setBarNumber(lawyer.getBarNumber())
            .setSpecialtyCode(lawyer.getSpecialty().getCode())
            .setCity(lawyer.getCity())
            .build();

        LawyerRegisteredV1 event = LawyerRegisteredV1.newBuilder()
            .setEventId(UUID.randomUUID())
            .setEventType("lawyer.registered")
            .setSchemaVersion(1)
            .setOccurredAt(Instant.now())
            .setAggregateId(user.getId())
            .setPayload(payload)
            .build();

        OutboxEvent outboxEvent = OutboxEvent.builder()
            .aggregateType("LAWYER")
            .aggregateId(user.getId())
            .eventType("lawyer.registered")
            .payload(avroSerializer.toBytes(event))
            .build();

        outboxRepository.save(outboxEvent);
    }

    // ─── Wrapper interne login/refresh ─────────────────────────────────
    public record LoginResult(String accessToken, String refreshToken, LoginResponse response) {}
}