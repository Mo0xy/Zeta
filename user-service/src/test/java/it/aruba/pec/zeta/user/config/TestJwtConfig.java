package it.aruba.pec.zeta.user.config;

import it.aruba.pec.zeta.user.security.JwtAuthenticationFilter;
import it.aruba.pec.zeta.user.security.JwtTokenProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@TestConfiguration
public class TestJwtConfig {

    @Bean
    @Primary
    public JwtAuthenticationFilter testJwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        return new JwtAuthenticationFilter(jwtTokenProvider) {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain)
                    throws ServletException, IOException {
                // Non fa autenticazione, passa solo la richiesta avanti
                filterChain.doFilter(request, response);
            }
        };
    }
}
