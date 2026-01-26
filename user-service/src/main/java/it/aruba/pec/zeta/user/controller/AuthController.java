package it.aruba.pec.zeta.user.controller;

import it.aruba.pec.zeta.common.dto.ApiResponse;
import it.aruba.pec.zeta.common.dto.LoginRequestDTO;
import it.aruba.pec.zeta.common.dto.LoginResponseDTO;
import it.aruba.pec.zeta.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller per l'autenticazione.
 * Endpoint pubblici (non richiedono token):
 * - POST /api/auth/login
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Effettua il login e restituisce il token JWT.
     *
     * @param request DTO con username e password
     * @return DTO con token JWT e dati utente
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {

        log.info("POST /api/auth/login - username: {}", request.getUsername());

        LoginResponseDTO response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.successLogin(response.getAccessToken(), "Login effettuato con successo"));
    }

}