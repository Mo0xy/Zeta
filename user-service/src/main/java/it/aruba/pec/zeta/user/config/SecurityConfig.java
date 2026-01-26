package it.aruba.pec.zeta.user.config;

import it.aruba.pec.zeta.user.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configurazione Spring Security.
 * Definisce:
 * - Quali endpoint sono pubblici vs protetti
 * - Il filtro JWT per l'autenticazione
 * - La policy di sessione (stateless)
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http

                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Regole di autorizzazione
                .authorizeHttpRequests(auth -> auth
                        // Endpoint pubblici
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // Tutto il resto richiede autenticazione
                        .anyRequest().authenticated()
                )

                // Aggiunge il filtro JWT PRIMA del filtro standard di Spring
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}

/**
 * Questa configurazione disabilita la protezione CSRF perché l’applicazione espone
 * API REST stateless che usano JWT nell’header Authorization.
 * In questo scenario il browser non invia automaticamente le credenziali,
 * quindi l’attacco CSRF non è applicabile. La protezione CSRF è necessaria
 * solo quando l’autenticazione si basa su cookie o sessioni.
 */