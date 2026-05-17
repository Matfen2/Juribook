package juribook.auth_service.controller;

import jakarta.validation.Valid;
import juribook.auth_service.dto.RegisterClientRequest;
import juribook.auth_service.dto.RegisterLawyerRequest;
import juribook.auth_service.dto.RegisterLawyerResponse;
import juribook.auth_service.dto.RegisterResponse;
import juribook.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
}