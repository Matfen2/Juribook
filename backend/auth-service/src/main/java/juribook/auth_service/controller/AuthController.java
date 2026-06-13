package juribook.auth_service.controller;

import jakarta.validation.Valid;
import juribook.auth_service.dto.request.CreateLawyerRequest;
import juribook.auth_service.dto.request.CreateUserRequest;
import juribook.auth_service.dto.response.RegisterLawyerResponse;
import juribook.auth_service.dto.response.UserResponse;
import juribook.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST exposant les endpoints d'authentification et de gestion
 * des comptes utilisateurs (auth-service).
 *
 * Toutes les routes sont préfixées par "/api/auth".
 *
 * @RequiredArgsConstructor (Lombok) génère un constructeur injectant
 * automatiquement AuthService (champ final ci-dessous).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    /**
     * Inscription d'un client (rôle CLIENT assigné automatiquement par AuthService).
     *
     * POST /api/auth/register
     * - @Valid déclenche les contraintes Jakarta de CreateUserRequest
     *   (email, password, name...) -> 400 Bad Request si invalide.
     * - @ResponseStatus(CREATED) -> renvoie 201, conformément au critère
     *   d'acceptation de la tâche 1.2.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody CreateUserRequest request) {
        return authService.create(request);
    }

    /**
     * Récupère un utilisateur par son identifiant.
     *
     * GET /api/auth/{id}
     * - Renvoie 404 (via UserNotFoundException + GlobalExceptionHandler)
     *   si l'id n'existe pas.
     *
     * TEMPORAIRE : actuellement ouvert (permitAll dans SecurityConfig) car
     * aucun JWT n'est encore en place. À restreindre (ex: ADMIN ou
     * propriétaire du compte) une fois 1.4/1.5 implémentés.
     */
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return authService.getById(id);
    }

    /**
     * Récupère la liste des utilisateurs, avec filtre optionnel par nom.
     *
     * GET /api/auth                -> tous les utilisateurs
     * GET /api/auth?name=Jean      -> utilisateurs dont le nom contient "Jean"
     *                                  (recherche insensible à la casse)
     *
     * TEMPORAIRE : même remarque que getById, à restreindre côté ADMIN
     * une fois la sécurité par rôle (JWT) en place.
     */
    @GetMapping
    public List<UserResponse> getAll(@RequestParam(required = false) String name) {
        return authService.getAll(name);
    }

    @PostMapping("/register/lawyer")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterLawyerResponse registerLawyer(@Valid @RequestBody CreateLawyerRequest request) {
        return authService.registerLawyer(request);
    }
}