package it.aruba.pec.zeta.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO per la risposta challenge di Aruba.
 * Il server Aruba restituisce questi dati che il client
 * deve usare per calcolare la firma HMAC.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArubaAuthChallengeDTO {

    /**
     * Timestamp generato dal server.
     * Usato per evitare replay attack (la firma scade dopo pochi minuti).
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS")
    private LocalDateTime timestamp;

    /**
     * Valore random univoco generato dal server.
     * Garantisce che ogni challenge sia diverso (anti-replay).
     */
    private String nonce;

    /**
     * Identificativo del client che ha richiesto il challenge.
     */
    @JsonProperty("client_id")
    private String clientId;
}