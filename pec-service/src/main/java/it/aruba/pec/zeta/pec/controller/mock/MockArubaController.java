package it.aruba.pec.zeta.pec.controller.mock;

import it.aruba.pec.zeta.common.dto.ArubaAuthChallengeDTO;
import it.aruba.pec.zeta.common.dto.PecInboxResponseDTO;
import it.aruba.pec.zeta.common.dto.PecMessageDTO;
import it.aruba.pec.zeta.pec.util.ArubaHmacUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller che simula gli endpoint dei server Aruba.
 *
 * In produzione questi endpoint sarebbero su server Aruba reali.
 * Qui li mockiamo per dimostrare il flusso di autenticazione HMAC.
 */
@RestController
@RequestMapping("/mock/aruba")
@Slf4j
public class MockArubaController {

    @Value("${aruba.mock.client-secret}")
    private String clientSecret;

    @Value("${aruba.mock.challenge-validity-seconds}")
    private int challengeValiditySeconds;

    // Memorizza i challenge emessi (in produzione sarebbe Redis/DB)
    private final Map<String, ArubaAuthChallengeDTO> activeChallenge = new ConcurrentHashMap<>();

    /**
     * Endpoint 1: Richiedi un challenge per l'autenticazione.
     *
     * GET /mock/aruba/challenge?clientId=xxx
     */
    @GetMapping("/challenge")
    public ResponseEntity<ArubaAuthChallengeDTO> getChallenge(
            @RequestParam("clientId") String clientId) {

        log.info("Challenge richiesto da client: {}", clientId);

        // Genera il challenge
        ArubaAuthChallengeDTO challenge = ArubaAuthChallengeDTO.builder()
                .timestamp(LocalDateTime.now())
                .nonce(ArubaHmacUtils.generateNonce())
                .clientId(clientId)
                .build();

        // Salva il challenge (per verificarlo dopo)
        activeChallenge.put(clientId, challenge);

        log.debug("Challenge generato: timestamp={}, nonce={}",
                challenge.getTimestamp(), challenge.getNonce());

        return ResponseEntity.ok(challenge);
    }

    /**
     * Endpoint 2: Ottieni i messaggi PEC (richiede autenticazione HMAC).
     *
     * GET /mock/aruba/messages
     * Headers:
     *   X-Client-Id: xxx
     *   X-Signature: xxx (HMAC calcolato)
     */
    @GetMapping("/messages")
    public ResponseEntity<?> getMessages(
            @RequestHeader("X-Client-Id") String clientId,
            @RequestHeader("X-Signature") String signature) {

        log.info("Richiesta messaggi da client: {}", clientId);

        // 1. Recupera il challenge attivo per questo client
        ArubaAuthChallengeDTO challenge = activeChallenge.get(clientId);

        if (challenge == null) {
            log.warn("Nessun challenge attivo per client: {}", clientId);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Nessun challenge attivo. Richiedere prima /challenge"));
        }

        // 2. Verifica che il challenge non sia scaduto
        if (challenge.getTimestamp().plusSeconds(challengeValiditySeconds).isBefore(LocalDateTime.now())) {
            log.warn("Challenge scaduto per client: {}", clientId);
            activeChallenge.remove(clientId);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Challenge scaduto. Richiederne uno nuovo."));
        }

        // 3. Verifica la firma HMAC
        boolean validSignature = ArubaHmacUtils.verifySignature(
                signature,
                challenge.getTimestamp(),
                challenge.getNonce(),
                challenge.getClientId(),
                clientSecret
        );

        if (!validSignature) {
            log.warn("Firma non valida per client: {}", clientId);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Firma non valida"));
        }

        // 4. Firma valida! Invalida il challenge (uso singolo)
        activeChallenge.remove(clientId);

        log.info("Autenticazione riuscita per client: {}", clientId);

        // 5. Restituisce i messaggi mock
        PecInboxResponseDTO response = buildMockInbox();

        return ResponseEntity.ok(response);
    }

    /**
     * Costruisce una inbox PEC con dati mock.
     */
    private PecInboxResponseDTO buildMockInbox() {
        List<PecMessageDTO> messages = List.of(
                PecMessageDTO.builder()
                        .id(1L)
                        .messageId("MSG-001-2026")
                        .sender("agenzia.entrate@pec.governo.it")
                        .recipients(List.of("mario.rossi@pec.aruba.it"))
                        .subject("Comunicazione fiscale anno 2025")
                        .body("Si comunica che la dichiarazione è stata ricevuta.")
                        .status(PecMessageDTO.MessageStatus.RECEIVED)
                        .receivedAt(LocalDateTime.now().minusDays(2))
                        .build(),

                PecMessageDTO.builder()
                        .id(2L)
                        .messageId("MSG-002-2026")
                        .sender("comune.roma@pec.it")
                        .recipients(List.of("mario.rossi@pec.aruba.it"))
                        .subject("Conferma iscrizione anagrafe")
                        .body("La sua richiesta è stata elaborata con successo.")
                        .status(PecMessageDTO.MessageStatus.READ)
                        .receivedAt(LocalDateTime.now().minusDays(5))
                        .build(),

                PecMessageDTO.builder()
                        .id(3L)
                        .messageId("MSG-003-2026")
                        .sender("info@pec.fornitore.it")
                        .recipients(List.of("mario.rossi@pec.aruba.it"))
                        .subject("Fattura n. 2026/001")
                        .body("In allegato la fattura del mese corrente.")
                        .status(PecMessageDTO.MessageStatus.DELIVERED)
                        .sentAt(LocalDateTime.now().minusDays(1))
                        .receivedAt(LocalDateTime.now().minusDays(1))
                        .build()
        );

        return PecInboxResponseDTO.builder()
                .messages(messages)
                .totalCount(messages.size())
                .mailboxAddress("mario.rossi@pec.aruba.it")
                .build();
    }
}
