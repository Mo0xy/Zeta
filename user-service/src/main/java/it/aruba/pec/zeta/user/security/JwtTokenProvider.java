package it.aruba.pec.zeta.user.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Provider per la generazione e validazione dei token JWT.
 *
 * Gestisce:
 * - Generazione di access token (breve durata)
 * - Generazione di refresh token (lunga durata)
 * - Validazione e parsing dei token
 * - Estrazione delle informazioni utente dal token
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Getter
    @Value("${jwt.access-token-expiration-ms:900000}") // 15 minuti default
    private long accessTokenExpirationMs;

    @Getter
    @Value("${jwt.refresh-token-expiration-ms:604800000}") // 7 giorni default
    private long refreshTokenExpirationMs;

    @Value("${jwt.issuer:piattaforma-zeta}")
    private String issuer;

    private SecretKey secretKey;

    /**
     * Inizializza la chiave segreta per la firma dei token.
     * Viene eseguito dopo l'iniezione delle dipendenze.
     */
    @PostConstruct
    public void init() {
        // Genera la chiave segreta a partire dalla stringa configurata
        // La chiave deve essere almeno 256 bit per HS256
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        log.info("JwtTokenProvider inizializzato - issuer: {}", issuer);
    }

    public String generateAccessToken(Long userId, String username, String email /* String role*/) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        // claims.put("role", role);
        claims.put("type", "access");

        String token = Jwts.builder()
                .issuer(issuer)
                .subject(username)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();

        log.debug("Access token generato per utente: {} - scadenza: {}", username, expiryDate);
        return token;
    }

    // per il futuro
    public String generateRefreshToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");

        String token = Jwts.builder()
                .issuer(issuer)
                .subject(username)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();

        log.debug("Refresh token generato per utente: {} - scadenza: {}", username, expiryDate);
        return token;
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    //Estrae l'ID utente dal token JWT.
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    //Estrae il ruolo dell'utente dal token JWT.
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    //Estrae il tipo di token (access/refresh).
    public String getTokenType(String token) {
        Claims claims = parseToken(token);
        return claims.get("type", String.class);
    }

    // Valida un token JWT verificando firma e scadenza.
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (SecurityException e) {
            log.error("Firma JWT non valida: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Token JWT malformato: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Token JWT scaduto: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT non supportato: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims vuoti: {}", e.getMessage());
        }
        return false;
    }

    public boolean isAccessToken(String token) {
        try {
            String type = getTokenType(token);
            return "access".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            String type = getTokenType(token);
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    // Restituisce il tempo rimanente alla scadenza del token in millisecondi
    public long getExpirationMs(String token) {
        Claims claims = parseToken(token);
        Date expiration = claims.getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    // parsa il token e restituisce i claims
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}