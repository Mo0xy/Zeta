package it.aruba.pec.zeta.pec.service;

import it.aruba.pec.zeta.common.dto.ArubaAuthChallengeDTO;
import it.aruba.pec.zeta.common.dto.PecInboxResponseDTO;
import it.aruba.pec.zeta.common.exception.ExternalServiceException;
import it.aruba.pec.zeta.pec.util.ArubaHmacUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Service per comunicare con i server Aruba (mockati).
 *
 * Gestisce il flusso di autenticazione HMAC:
 * 1. Richiede il challenge
 * 2. Calcola la firma
 * 3. Chiama gli endpoint protetti
 */
@Service
@Slf4j
public class ArubaClientService {

    private final WebClient webClient;

    @Value("${aruba.mock.client-id}")
    private String clientId;

    @Value("${aruba.mock.client-secret}")
    private String clientSecret;

    @Value("${aruba.mock.base-url}")
    private String baseUrl;

    public ArubaClientService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Recupera i messaggi PEC dalla casella.
     *
     * Flusso:
     * 1. GET /challenge → ottiene timestamp, nonce, clientId
     * 2. Calcola HMAC(timestamp|nonce|clientId, secret)
     * 3. GET /messages con header X-Signature
     *
     * @return Lista messaggi PEC
     * @throws ExternalServiceException se la comunicazione fallisce
     */
    public PecInboxResponseDTO getInbox() {
        log.info("Inizio recupero inbox PEC da Aruba");

        try {
            // STEP 1: Richiede il challenge
            log.debug("Step 1: Richiesta challenge a {}/challenge", baseUrl);

            ArubaAuthChallengeDTO challenge = webClient.get()
                    .uri(baseUrl + "/challenge?clientId=" + clientId)
                    .retrieve()
                    .bodyToMono(ArubaAuthChallengeDTO.class)
                    .block();

            if (challenge == null) {
                throw new ExternalServiceException("Aruba", "Challenge nullo ricevuto");
            }

            log.debug("Challenge ricevuto: timestamp={}, nonce={}",
                    challenge.getTimestamp(), challenge.getNonce());

            // STEP 2: Calcola la firma HMAC
            log.debug("Step 2: Calcolo firma HMAC");

            String signature = ArubaHmacUtils.calculateSignature(
                    challenge.getTimestamp(),
                    challenge.getNonce(),
                    challenge.getClientId(),
                    clientSecret
            );

            log.debug("Firma calcolata: {}", signature);

            // STEP 3: Richiedi i messaggi con la firma
            log.debug("Step 3: Richiesta messaggi a {}/messages", baseUrl);

            PecInboxResponseDTO inbox = webClient.get()
                    .uri(baseUrl + "/messages")
                    .header("X-Client-Id", clientId)
                    .header("X-Signature", signature)
                    .retrieve()
                    .bodyToMono(PecInboxResponseDTO.class)
                    .block();

            if (inbox == null) {
                throw new ExternalServiceException("Aruba", "Risposta inbox nulla");
            }

            log.info("Inbox recuperata con successo: {} messaggi", inbox.getTotalCount());

            return inbox;

        } catch (WebClientResponseException e) {
            log.error("Errore HTTP da Aruba: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalServiceException("Aruba",
                    "Errore comunicazione: " + e.getMessage(),
                    e.getStatusCode().value());

        } catch (Exception e) {
            log.error("Errore durante comunicazione con Aruba: {}", e.getMessage());
            throw new ExternalServiceException("Aruba", e.getMessage(), e);
        }
    }
}
