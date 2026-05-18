package juribook.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import juribook.auth_service.dto.LoginRequest;
import juribook.auth_service.dto.RegisterClientRequest;
import juribook.auth_service.model.entity.Role;
import juribook.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("AuthController — tests d'intégration")
class AuthControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("auth_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled",      () -> "true");
    }

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    // ─── Test 6: inscription nominale → 201 + persistance ──────────────
    @Test
    @DisplayName("POST /api/auth/register — retourne 201 et persiste le user")
    void register_returns201_andPersistsUser() throws Exception {
        RegisterClientRequest request = new RegisterClientRequest(
            "alice@test.com", "Password1", "Alice", "Martin", "0612345678"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(notNullValue()))
            .andExpect(jsonPath("$.email").value("alice@test.com"))
            .andExpect(jsonPath("$.firstName").value("Alice"));

        // Vérification BDD
        assertThat(userRepository.existsByEmail("alice@test.com")).isTrue();
        var alice = userRepository.findByEmail("alice@test.com").orElseThrow();
        assertThat(alice.getPasswordHash()).startsWith("$2a$");
        assertThat(alice.getRoles())
            .extracting(role -> role.getName())
            .containsExactly(Role.RoleName.CLIENT);
    }

    // ─── Test 7: route protégée sans cookie → 401 ──────────────────────
    @Test
    @DisplayName("GET /api/auth/me sans cookie JWT — retourne 401")
    void protectedRoute_returns401_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    // ─── Test 8: route LAWYER avec cookie CLIENT → 403 ─────────────────
    @Test
    @DisplayName("GET /api/auth/me/lawyer-only avec cookie CLIENT — retourne 403")
    void lawyerOnlyRoute_returns403_forClient() throws Exception {
        // 1. Inscrire un client
        RegisterClientRequest registerReq = new RegisterClientRequest(
            "alice@test.com", "Password1", "Alice", "Martin", null
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)))
            .andExpect(status().isCreated());

        // 2. Logger ce client pour récupérer le cookie JWT
        LoginRequest loginReq = new LoginRequest("alice@test.com", "Password1");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
            .andExpect(status().isOk())
            .andReturn();

        Cookie accessCookie = loginResult.getResponse().getCookie("juribook_access_token");
        assertThat(accessCookie).isNotNull();

        // 3. Tenter d'accéder à un endpoint LAWYER avec le cookie CLIENT → 403
        mockMvc.perform(get("/api/auth/me/lawyer-only").cookie(accessCookie))
            .andExpect(status().isForbidden());

        // 4. Sanity check : ce même cookie accède bien à client-only → 200
        mockMvc.perform(get("/api/auth/me/client-only").cookie(accessCookie))
            .andExpect(status().isOk());
    }
}