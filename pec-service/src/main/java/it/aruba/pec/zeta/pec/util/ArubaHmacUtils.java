package it.aruba.pec.zeta.pec.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * Utility per la generazione e verifica di firme HMAC.
 *
 * Usato per l'autenticazione con i server Aruba (mockati).
 */
@Slf4j
public final class ArubaHmacUtils {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private ArubaHmacUtils() {
        // Utility class
    }

    /**
     * Genera un nonce random univoco.
     *
     * @return Stringa UUID random
     */
    public static String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Calcola la firma HMAC-SHA256.
     *
     * @param timestamp Timestamp del challenge
     * @param nonce     Nonce del challenge
     * @param clientId  Client ID
     * @param secret    Chiave segreta condivisa
     * @return Firma in Base64
     */
    public static String calculateSignature(LocalDateTime timestamp, String nonce,
                                            String clientId, String secret) {
        // Costruisce il messaggio da firmare
        String message = buildMessage(timestamp, nonce, clientId);

        log.debug("Messaggio da firmare: {}", message);

        return hmacSha256(message, secret);
    }

    /**
     * Verifica che la firma sia valida.
     *
     * @param signature Firma ricevuta dal client
     * @param timestamp Timestamp del challenge
     * @param nonce     Nonce del challenge
     * @param clientId  Client ID
     * @param secret    Chiave segreta condivisa
     * @return true se la firma è valida
     */
    public static boolean verifySignature(String signature, LocalDateTime timestamp,
                                          String nonce, String clientId, String secret) {
        String expectedSignature = calculateSignature(timestamp, nonce, clientId, secret);

        boolean valid = expectedSignature.equals(signature);

        if (!valid) {
            log.warn("Firma non valida. Attesa: {}, Ricevuta: {}", expectedSignature, signature);
        }

        return valid;
    }

    /**
     * Costruisce il messaggio da firmare concatenando i parametri.
     */
    private static String buildMessage(LocalDateTime timestamp, String nonce, String clientId) {
        return timestamp.toString() + "|" + nonce + "|" + clientId;
    }

    /**
     * Calcola HMAC-SHA256 e restituisce il risultato in Base64.
     */
    private static String hmacSha256(String message, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );
            mac.init(secretKeySpec);

            byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hmacBytes);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Errore nel calcolo HMAC: {}", e.getMessage());
            throw new RuntimeException("Errore nel calcolo della firma HMAC", e);
        }
    }
}
