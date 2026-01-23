package it.aruba.pec.zeta.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO per la richiesta di login.

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "Username è obbligatorio")
    private String username;

    @NotBlank(message = "Password è obbligatoria")
    private String password;
}