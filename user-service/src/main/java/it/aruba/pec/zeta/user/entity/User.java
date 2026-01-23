package it.aruba.pec.zeta.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 20)
//    @Builder.Default
//    private UserRole role = UserRole.USER;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserServiceCredential> serviceCredentials = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

//    public enum UserRole {
//        USER,
//        ADMIN
//    }

    // Helper method per aggiungere credenziali servizio
    public void addServiceCredential(UserServiceCredential credential) {
        serviceCredentials.add(credential);
        credential.setUser(this);
    }

    public void removeServiceCredential(UserServiceCredential credential) {
        serviceCredentials.remove(credential);
        credential.setUser(null);
    }
}