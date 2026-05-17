package juribook.auth_service.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import juribook.auth_service.config.JwtProperties;
import juribook.auth_service.dto.CurrentUserResponse;
import juribook.auth_service.dto.LoginRequest;
import juribook.auth_service.dto.LoginResponse;
import juribook.auth_service.dto.RegisterClientRequest;
import juribook.auth_service.dto.RegisterLawyerRequest;
import juribook.auth_service.dto.RegisterLawyerResponse;
import juribook.auth_service.dto.RegisterResponse;
import juribook.auth_service.model.entity.User;
import juribook.auth_service.repository.UserRepository;
import juribook.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    // ─── Inscription client ─────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerClient(
            @Valid @RequestBody RegisterClientRequest request) {
        RegisterResponse response = authService.registerClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─── Inscription avocat ─────────────────────────────────────────────
    @PostMapping("/register/lawyer")
    public ResponseEntity<RegisterLawyerResponse> registerLawyer(
            @Valid @RequestBody RegisterLawyerRequest request) {
        RegisterLawyerResponse response = authService.registerLawyer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─── Login ──────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        AuthService.LoginResult result = authService.login(request, httpRequest);
        setAuthCookies(httpResponse, result.accessToken(), result.refreshToken());
        return ResponseEntity.ok(result.response());
    }

    // ─── Refresh : rotation du refresh token (1.7) ──────────────────────
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = "${juribook.jwt.refresh-cookie-name}", required = false) String refreshToken,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            AuthService.LoginResult result = authService.refresh(refreshToken, httpRequest);
            setAuthCookies(httpResponse, result.accessToken(), result.refreshToken());
            return ResponseEntity.ok(result.response());
        } catch (IllegalStateException e) {
            clearAuthCookies(httpResponse);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // ─── Logout : révoque les refresh tokens + clear cookies (1.7) ─────
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication,
            HttpServletResponse httpResponse) {

        if (authentication != null) {
            userRepository.findByEmail(authentication.getName())
                .ifPresent(user -> authService.logout(user.getId()));
        }
        clearAuthCookies(httpResponse);
        return ResponseEntity.noContent().build();
    }

    // ─── /me — Profil de l'utilisateur authentifié (tout rôle confondu) ─
    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(auth -> auth.replace("ROLE_", ""))   // ROLE_CLIENT → CLIENT
            .toList();

        return ResponseEntity.ok(new CurrentUserResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            roles
        ));
    }

    // ─── Endpoint réservé aux CLIENTs ───────────────────────────────────
    @GetMapping("/me/client-only")
    public ResponseEntity<String> clientOnly(Authentication authentication) {
        return ResponseEntity.ok("Bienvenue cher client " + authentication.getName());
    }

    // ─── Endpoint réservé aux LAWYERs ───────────────────────────────────
    @GetMapping("/me/lawyer-only")
    public ResponseEntity<String> lawyerOnly(Authentication authentication) {
        return ResponseEntity.ok("Bienvenue Maître " + authentication.getName());
    }

    // ─── Endpoint réservé aux ADMINs ────────────────────────────────────
    @GetMapping("/me/admin-only")
    public ResponseEntity<String> adminOnly(Authentication authentication) {
        return ResponseEntity.ok("Bienvenue administrateur " + authentication.getName());
    }

    // ─── Endpoint accessible aux LAWYERs OU ADMINs (modération etc.) ────
    @GetMapping("/me/lawyer-or-admin")
    public ResponseEntity<String> lawyerOrAdmin(Authentication authentication) {
        return ResponseEntity.ok("Zone réservée modération: " + authentication.getName());
    }

    // ─── Helpers privés (gestion des cookies access + refresh) ──────────

    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Cookie access token (1h)
        Cookie accessCookie = new Cookie(jwtProperties.cookieName(), accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);   // ← true en HTTPS (Sprint 8)
        accessCookie.setPath("/");
        accessCookie.setMaxAge((int) (jwtProperties.expirationMs() / 1000));
        accessCookie.setAttribute("SameSite", "Strict");
        response.addCookie(accessCookie);

        // Cookie refresh token (7j) — Path restreint à /api/auth pour limiter l'exposition
        Cookie refreshCookie = new Cookie(jwtProperties.refreshCookieName(), refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) (jwtProperties.refreshExpirationMs() / 1000));
        refreshCookie.setAttribute("SameSite", "Strict");
        response.addCookie(refreshCookie);
    }

    private void clearAuthCookies(HttpServletResponse response) {
        Cookie access = new Cookie(jwtProperties.cookieName(), "");
        access.setHttpOnly(true);
        access.setPath("/");
        access.setMaxAge(0);
        response.addCookie(access);

        Cookie refresh = new Cookie(jwtProperties.refreshCookieName(), "");
        refresh.setHttpOnly(true);
        refresh.setPath("/");
        refresh.setMaxAge(0);
        response.addCookie(refresh);
    }
}