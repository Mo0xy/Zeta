package it.aruba.pec.zeta.pec.controller;

import it.aruba.pec.zeta.common.dto.ApiResponse;
import it.aruba.pec.zeta.common.dto.PecInboxResponseDTO;
import it.aruba.pec.zeta.pec.service.PecService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST per le operazioni PEC.
 *
 * Espone gli endpoint per i client esterni.
 */
@RestController
@RequestMapping("/api/pec")
@RequiredArgsConstructor
@Slf4j
public class PecController {

    private final PecService pecService;

    /**
     * Recupera la inbox PEC.
     *
     * GET /api/pec/inbox
     *
     * @return Lista messaggi PEC
     */
    @GetMapping("/inbox")
    public ResponseEntity<ApiResponse<PecInboxResponseDTO>> getInbox() {
        log.info("GET /api/pec/inbox");

        PecInboxResponseDTO inbox = pecService.getInbox();

        return ResponseEntity.ok(ApiResponse.success(inbox));
    }
}
