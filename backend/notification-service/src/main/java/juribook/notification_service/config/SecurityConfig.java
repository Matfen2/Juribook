package juribook.notification_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactive CSRF : inutile pour une API REST stateless (pas de formulaires HTML)
            .csrf(csrf -> csrf.disable())

            // Pas de session HTTP : chaque requête est authentifiée indépendamment (JWT plus tard)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // Swagger / OpenAPI accessibles sans authentification (dev)
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml"
                ).permitAll()

                // Actuator (health check) accessible sans authentification
                .requestMatchers("/actuator/**").permitAll()

                // Toutes les autres routes nécessitent une authentification
                .anyRequest().authenticated()
            )

            // Désactive le formulaire de login par défaut (on utilisera JWT plus tard)
            .formLogin(form -> form.disable())

            // Désactive l'authentification HTTP Basic
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
