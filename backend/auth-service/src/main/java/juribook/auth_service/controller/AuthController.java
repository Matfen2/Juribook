package juribook.auth_service.controller;

import jakarta.validation.Valid;
import juribook.auth_service.dto.request.CreateUserRequest;
import juribook.auth_service.dto.request.LoginRequest;
import juribook.auth_service.dto.response.LoginResponse;
import juribook.auth_service.dto.response.UserResponse;
import juribook.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Création d'un nouveau compte utilisateur
    // POST /api/auth/register
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody CreateUserRequest request) {
        return authService.create(request);
    }

    // Authentification : vérifie email/mot de passe et renvoie un token JWT
    // POST /api/auth/login
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // Obtenir un utilisateur spécifique
    // GET /api/auth/{id}
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return authService.getById(id);
    }

    // Obtenir tous les utilisateurs (filtre optionnel par nom)
    // GET /api/auth?name=...
    @GetMapping
    public List<UserResponse> getAll(@RequestParam(required = false) String name) {
        return authService.getAll(name);
    }
}