package juribook.lawyer_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration Spring Security de lawyer-service.
 *
 * Sans cette classe, Spring Security applique sa configuration par défaut
 * (formulaire de login + mot de passe généré aléatoirement), ce qui
 * bloquerait Swagger UI et tous les endpoints en 403/login.
 *
 * SessionCreationPolicy.STATELESS : pas de session HTTP côté serveur,
 * cohérent avec une authentification future par JWT (1.4/1.5) -
 * chaque requête sera authentifiée indépendamment via son token.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF inutile pour une API REST stateless (pas de formulaires HTML)
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // TEMPORAIRE - consultation publique des profils avocats,
                // avant mise en place du JWT (1.4/1.5). Pourrait rester
                // public à terme (un visiteur non connecté doit pouvoir
                // consulter les profils, cf. cahier des charges - acteur "Visiteur"),
                // mais à réévaluer une fois la sécurité par rôle en place.
                .requestMatchers(HttpMethod.GET, "/api/lawyers/**").permitAll()

                // Swagger / OpenAPI accessibles sans authentification (dev)
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml"
                ).permitAll()

                // Actuator (health check) accessible sans authentification
                .requestMatchers("/actuator/**").permitAll()

                // Toutes les autres routes (futures POST/PATCH admin)
                // nécessiteront un JWT valide.
                .anyRequest().authenticated()
            )

            // Pas de formulaire de login par défaut : remplacé par JWT
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}