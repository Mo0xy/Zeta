package it.aruba.pec.zeta.pec.service;

import it.aruba.pec.zeta.common.dto.PecInboxResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service per la gestione delle operazioni PEC.
 *
 * Coordina le chiamate verso i server Aruba e applica
 * eventuali logiche di business.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PecService {

    private final ArubaClientService arubaClientService;

    /**
     * Recupera la inbox PEC dell'utente.
     *
     * @return Inbox con lista messaggi
     */
    public PecInboxResponseDTO getInbox() {
        log.info("Recupero inbox PEC");

        PecInboxResponseDTO inbox = arubaClientService.getInbox();

        log.info("Inbox recuperata: {} messaggi per {}",
                inbox.getTotalCount(),
                inbox.getMailboxAddress());

        return inbox;
    }
}