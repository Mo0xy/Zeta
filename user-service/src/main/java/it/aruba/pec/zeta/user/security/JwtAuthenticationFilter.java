package it.aruba.pec.zeta.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro che intercetta ogni richiesta HTTP per validare il token JWT.
 *
 * Flusso:
 * 1. Estrae il token dall'header "Authorization: Bearer xxx"
 * 2. Valida il token
 * 3. Se valido, imposta l'utente nel SecurityContext
 * 4. Passa la richiesta al controller
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

                // Verifica che sia un access token
                if (!jwtTokenProvider.isAccessToken(jwt)) {
                    log.warn("Tentativo di accesso con refresh token");
                    filterChain.doFilter(request, response);
                    return;
                }

                // Estrae i dati utente dal token
                Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
                String username = jwtTokenProvider.getUsernameFromToken(jwt);

                // Crea l'oggetto che rappresenta l'utente autenticato
                JwtUserDetails userDetails = JwtUserDetails.builder()
                        .userId(userId)
                        .username(username)
                        .build();

                // Crea l'oggetto Authentication per Spring Security
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                Collections.emptyList()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Imposta l'utente nel SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Utente autenticato: {} (ID: {})", username, userId);
            }
        } catch (Exception e) {
            log.error("Errore durante l'autenticazione JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Estrae il token JWT dall'header Authorization.
     * Formato atteso: "Authorization: Bearer eyJhbG..."
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}