package it.aruba.pec.zeta.user.service;

import it.aruba.pec.zeta.common.dto.LoginRequestDTO;
import it.aruba.pec.zeta.common.dto.LoginResponseDTO;
import it.aruba.pec.zeta.common.dto.UserDTO;
import it.aruba.pec.zeta.common.exception.UnauthorizedException;
import it.aruba.pec.zeta.user.entity.User;
import it.aruba.pec.zeta.user.mapper.UserMapper;
import it.aruba.pec.zeta.user.repository.UserRepository;
import it.aruba.pec.zeta.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service per la gestione dell'autenticazione.
 *
 * Responsabilità:
 * - Verificare le credenziali utente
 * - Generare il token JWT
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Effettua il login dell'utente.
     *
     * @param request DTO con username e password
     * @return DTO con token JWT e dati utente
     * @throws UnauthorizedException se le credenziali non sono valide
     */
    public LoginResponseDTO login(LoginRequestDTO request) {
        log.debug("Tentativo di login per utente: {}", request.getUsername());
        log.debug("Password fornita: {}", request.getPassword());

        // 1. Cerca l'utente nel database
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Login fallito: utente '{}' non trovato", request.getUsername());
                    return new UnauthorizedException("Credenziali non valide");
                });

        // 2. Verifica che l'utente sia abilitato
        if (!user.isEnabled()) {
            log.warn("Login fallito: utente '{}' disabilitato", request.getUsername());
            throw new UnauthorizedException("Utente disabilitato");
        }

        // 3. Verifica la password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.info(user.getPasswordHash());
            log.warn("Login fallito: password errata per utente '{}'", request.getUsername());
            throw new UnauthorizedException("Credenziali non valide");
        }

        // 4. Genera il token JWT
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );

        log.info("Login effettuato con successo per utente: {}", request.getUsername());

        // 5. Costruisce la risposta
        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000) // in secondi
                .user(userMapper.toDTO(user))
                .build();
    }
}
