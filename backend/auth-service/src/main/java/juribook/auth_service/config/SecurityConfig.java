package juribook.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration Spring Security de auth-service.
 *
 * Sans cette classe, Spring Security applique sa configuration par défaut
 * (formulaire de login + mot de passe généré aléatoirement), ce qui
 * bloquerait Swagger UI et tous les endpoints en 403/login.
 *
 * SessionCreationPolicy.STATELESS : pas de session HTTP côté serveur,
 * cohérent avec une authentification future par JWT (1.4/1.5).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF inutile pour une API REST stateless
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // Inscription (client et avocat) et connexion : accessibles
                // sans authentification, un utilisateur non connecté doit
                // pouvoir créer un compte ou se logger.
                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/register/lawyer",
                    "/api/auth/login"
                ).permitAll()

                // TEMPORAIRE - consultation publique avant mise en place
                // du JWT (1.4/1.5). À restreindre côté ADMIN ensuite.
                .requestMatchers(HttpMethod.GET, "/api/auth/**").permitAll()

                // Swagger / OpenAPI accessibles sans authentification (dev)
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml"
                ).permitAll()

                // Actuator (health check) accessible sans authentification
                .requestMatchers("/actuator/**").permitAll()

                // Toutes les autres routes nécessiteront un JWT valide
                .anyRequest().authenticated()
            )

            // Pas de formulaire de login par défaut : remplacé par JWT
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }

    /**
     * Encodeur de mot de passe BCrypt, utilisé par AuthService
     * pour hacher les mots de passe avant stockage en base.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}