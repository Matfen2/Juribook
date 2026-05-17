package juribook.auth_service.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import juribook.auth_service.config.JwtProperties;
import juribook.auth_service.dto.LoginRequest;
import juribook.auth_service.dto.LoginResponse;
import juribook.auth_service.dto.RegisterClientRequest;
import juribook.auth_service.dto.RegisterLawyerRequest;
import juribook.auth_service.dto.RegisterLawyerResponse;
import juribook.auth_service.dto.RegisterResponse;
import juribook.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerClient(
            @Valid @RequestBody RegisterClientRequest request) {
        RegisterResponse response = authService.registerClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register/lawyer")
    public ResponseEntity<RegisterLawyerResponse> registerLawyer(
            @Valid @RequestBody RegisterLawyerRequest request) {
        RegisterLawyerResponse response = authService.registerLawyer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpResponse) {

        AuthService.LoginResult result = authService.login(request);

        // Pose le cookie HttpOnly avec le JWT
        Cookie jwtCookie = new Cookie(jwtProperties.cookieName(), result.token());
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);   // ← passer à true en HTTPS (Sprint 8)
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge((int) (jwtProperties.expirationMs() / 1000));
        jwtCookie.setAttribute("SameSite", "Strict");
        httpResponse.addCookie(jwtCookie);

        return ResponseEntity.ok(result.response());
    }
}