package it.aruba.pec.zeta.user.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Rappresenta i dati dell'utente autenticato estratti dal token JWT.
 * Questa classe viene usata come "principal" nel SecurityContext di Spring.
 * Permette di accedere alle informazioni dell'utente corrente in qualsiasi
 * punto dell'applicazione.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtUserDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String email;
    // private String role;


     // Verifica se l'utente ha il ruolo di amministratore.

//    public boolean isAdmin() {
//        return "ADMIN".equalsIgnoreCase(role);
//    }
}