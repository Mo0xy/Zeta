package it.aruba.pec.zeta.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_service_credentials")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class UserServiceCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ServiceType serviceType;

    @Column(nullable = false, length = 500)
    private String encryptedAccessToken;

    @Column(length = 500)
    private String encryptedRefreshToken;

    @Column(nullable = false)
    private LocalDateTime tokenExpiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum ServiceType {
        PEC,
        FIRMA_DIGITALE,
        CONSERVAZIONE,
        FATTURAZIONE
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(tokenExpiresAt);
    }
}