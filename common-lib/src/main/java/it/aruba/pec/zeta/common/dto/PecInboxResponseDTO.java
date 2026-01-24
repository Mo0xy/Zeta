package it.aruba.pec.zeta.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO per la risposta della inbox PEC.
 * Contiene la lista dei messaggi PEC e metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PecInboxResponseDTO {

    /**
     * Lista dei messaggi PEC.
     */
    private List<PecMessageDTO> messages;

    /**
     * Numero totale di messaggi.
     */
    @JsonProperty("total_count")
    private int totalCount;

    /**
     * Indirizzo email della casella PEC.
     */
    @JsonProperty("mailbox_address")
    private String mailboxAddress;
}