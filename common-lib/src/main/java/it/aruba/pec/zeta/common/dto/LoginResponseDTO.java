package it.aruba.pec.zeta.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la risposta di login.
 * Esempio di response body:
 * {
 *   "access_token": "eyJhbGciOiJIUzI1NiIs...",
 *   "token_type": "Bearer",
 *   "expires_in": 900,
 *   "user": { ... }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";

    @JsonProperty("expires_in")
    private long expiresIn; // secondi alla scadenza

    private UserDTO user;
}