package juribook.lawyer_service.controller;

import juribook.lawyer_service.dto.response.LawyerResponse;
import juribook.lawyer_service.service.LawyerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST de lawyer-service.
 *
 * Toutes les routes sont préfixées par "/api/lawyers".
 *
 * Sprint 1 : uniquement de la consultation (GET). La création est
 * implicite, via Kafka (LawyerRegistrationConsumer) - il n'existe
 * volontairement aucun endpoint POST de création de profil avocat ici :
 * ce serait un second chemin de création de compte, redondant avec
 * auth-service.
 */
@RestController
@RequestMapping("/api/lawyers")
@RequiredArgsConstructor
public class LawyerController {

    private final LawyerService lawyerService;

    /**
     * GET /api/lawyers/{id} - consultation d'un profil avocat par son id.
     * Renvoie 404 si l'id n'existe pas (LawyerNotFoundException + GlobalExceptionHandler).
     */
    @GetMapping("/{id}")
    public LawyerResponse getById(@PathVariable Long id) {
        return lawyerService.getById(id);
    }

    /**
     * GET /api/lawyers - liste de tous les profils avocats.
     * TEMPORAIRE : ouvert sans authentification (permitAll dans SecurityConfig),
     * à restreindre/affiner lorsque la recherche publique par filtres (2.3)
     * et la sécurité par rôle (1.4/1.5) seront en place.
     */
    @GetMapping
    public List<LawyerResponse> getAll() {
        return lawyerService.getAll();
    }
}