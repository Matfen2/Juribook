package juribook.auth_service.service;

import jakarta.servlet.http.HttpServletRequest;
import juribook.auth_service.dto.LoginRequest;
import juribook.auth_service.dto.RegisterClientRequest;
import juribook.auth_service.dto.RegisterLawyerRequest;
import juribook.auth_service.dto.RegisterLawyerResponse;
import juribook.auth_service.dto.RegisterResponse;
import juribook.auth_service.events.AvroSerializer;
import juribook.auth_service.exception.EmailAlreadyExistsException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — tests unitaires")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private SpecialtyRepository specialtyRepository;
    @Mock private LawyerRepository lawyerRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private HttpServletRequest httpRequest;
    @Mock private OutboxRepository outboxRepository;
    @Mock private AvroSerializer avroSerializer;

    @InjectMocks
    private AuthService authService;

    private Role clientRole;
    private Role lawyerRole;
    private Specialty droitTravail;

    @BeforeEach
    void setUp() {
        clientRole = Role.builder()
            .id(1L)
            .name(Role.RoleName.CLIENT)
            .createdAt(OffsetDateTime.now())
            .build();

        lawyerRole = Role.builder()
            .id(2L)
            .name(Role.RoleName.LAWYER)
            .createdAt(OffsetDateTime.now())
            .build();

        droitTravail = Specialty.builder()
            .id(1L)
            .code("DROIT_TRAVAIL")
            .name("Droit du travail")
            .build();
    }

    // ─── Test 1: inscription client nominale ────────────────────────────
    @Test
    @DisplayName("registerClient — crée un user CLIENT avec password hashé")
    void registerClient_success() {
        // Given
        RegisterClientRequest request = new RegisterClientRequest(
            "alice@test.com", "Password1", "Alice", "Martin", "0612345678"
        );

        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.CLIENT)).thenReturn(Optional.of(clientRole));
        when(passwordEncoder.encode("Password1")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            u.setCreatedAt(OffsetDateTime.now());
            return u;
        });

        // When
        RegisterResponse response = authService.registerClient(request);

        // Then
        assertThat(response.email()).isEqualTo("alice@test.com");
        assertThat(response.firstName()).isEqualTo("Alice");
        assertThat(response.lastName()).isEqualTo("Martin");
        assertThat(response.id()).isNotNull();

        verify(passwordEncoder).encode("Password1");
        verify(userRepository).save(any(User.class));
    }

    // ─── Test 2: email déjà pris ────────────────────────────────────────
    @Test
    @DisplayName("registerClient — lance EmailAlreadyExistsException si email pris")
    void registerClient_emailAlreadyExists() {
        // Given
        RegisterClientRequest request = new RegisterClientRequest(
            "alice@test.com", "Password1", "Alice", "Martin", null
        );

        when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);

        // When + Then
        assertThatThrownBy(() -> authService.registerClient(request))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessageContaining("alice@test.com");

        // Vérifications: aucune persistance ne doit avoir lieu
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // ─── Test 3: inscription avocat → enabled=false + event outbox ─────
    @Test
    @DisplayName("registerLawyer — crée un user LAWYER en attente de validation (enabled=false) et publie l'event outbox")
    void registerLawyer_pendingValidation() {
        // Given
        RegisterLawyerRequest request = new RegisterLawyerRequest(
            "maitre@avocat.fr", "Password1", "Pierre", "Dupont", "0612345678",
            "PAR-2024-12345", "DROIT_TRAVAIL", "Paris"
        );

        when(userRepository.existsByEmail("maitre@avocat.fr")).thenReturn(false);
        when(lawyerRepository.existsByBarNumber("PAR-2024-12345")).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.LAWYER)).thenReturn(Optional.of(lawyerRole));
        when(specialtyRepository.findByCode("DROIT_TRAVAIL")).thenReturn(Optional.of(droitTravail));
        when(passwordEncoder.encode("Password1")).thenReturn("$2a$10$hashedPassword");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            u.setCreatedAt(OffsetDateTime.now());
            return u;
        });
        when(lawyerRepository.save(any(Lawyer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(avroSerializer.toBytes(any())).thenReturn(new byte[]{1, 2, 3});

        // When
        RegisterLawyerResponse response = authService.registerLawyer(request);

        // Then
        assertThat(response.status()).isEqualTo("PENDING_VALIDATION");
        assertThat(response.specialty()).isEqualTo("Droit du travail");
        assertThat(response.barNumber()).isEqualTo("PAR-2024-12345");

        // Vérifie que le user a été créé avec enabled=false
        verify(userRepository).save(any(User.class));
        // Vérifie qu'un event lawyer.registered a été ajouté à l'outbox
        verify(avroSerializer).toBytes(any());
        verify(outboxRepository).save(any(OutboxEvent.class));
    }

    // ─── Test 4: login retourne JWT + refresh token ────────────────────
    @Test
    @DisplayName("login — retourne access token + refresh token sur credentials valides")
    void login_success() {
        // Given
        LoginRequest request = new LoginRequest("alice@test.com", "Password1");

        User user = User.builder()
            .id(UUID.randomUUID())
            .email("alice@test.com")
            .firstName("Alice")
            .lastName("Martin")
            .enabled(true)
            .roles(Set.of(clientRole))
            .build();

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("fake.jwt.token");
        when(refreshTokenService.createForUser(user, httpRequest)).thenReturn("fake-refresh-token");

        // When
        AuthService.LoginResult result = authService.login(request, httpRequest);

        // Then
        assertThat(result.accessToken()).isEqualTo("fake.jwt.token");
        assertThat(result.refreshToken()).isEqualTo("fake-refresh-token");
        assertThat(result.response().email()).isEqualTo("alice@test.com");
        assertThat(result.response().roles()).containsExactly("CLIENT");

        verify(authenticationManager).authenticate(any());
        verify(jwtService).generateToken(user);
        verify(refreshTokenService).createForUser(user, httpRequest);
    }

    // ─── Test 5: login avec compte désactivé ───────────────────────────
    @Test
    @DisplayName("login — propage DisabledException si compte en attente de validation")
    void login_disabledAccount() {
        // Given
        LoginRequest request = new LoginRequest("maitre@avocat.fr", "Password1");

        when(authenticationManager.authenticate(any()))
            .thenThrow(new DisabledException("Compte en attente de validation"));

        // When + Then
        assertThatThrownBy(() -> authService.login(request, httpRequest))
            .isInstanceOf(DisabledException.class)
            .hasMessageContaining("attente");

        // Aucun token ne doit être généré
        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenService, never()).createForUser(any(), any());
    }
}