package com.dvein.banking_backend.auth.model;

import com.dvein.banking_backend.common.enums.AuthenticationState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pre_auth_sessions")
@EntityListeners(AuditingEntityListener.class)
public class PreAuthenticationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "pre_auth_token", nullable = false, unique = true, length = 500)
    private String preAuthToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "authentication_state", nullable = false)
    private AuthenticationState authenticationState;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "device_verified", nullable = false)
    @Builder.Default
    private boolean deviceVerified = false;

    @Column(name = "totp_verified", nullable = false)
    @Builder.Default
    private boolean totpVerified = false;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void markDeviceVerified() {
        this.deviceVerified = true;
        updateAuthenticationState();
    }

    public void markTotpVerified() {
        this.totpVerified = true;
        updateAuthenticationState();
    }

    public void complete() {
        this.active = false;
        this.completedAt = LocalDateTime.now();
        this.authenticationState = AuthenticationState.FULLY_AUTHENTICATED;
    }

    private void updateAuthenticationState() {
        if (deviceVerified && totpVerified) {
            this.authenticationState = AuthenticationState.FULLY_AUTHENTICATED;
        } else if (deviceVerified && !totpVerified) {
            this.authenticationState = AuthenticationState.REQUIRES_TOTP;
        } else if (!deviceVerified) {
            this.authenticationState = AuthenticationState.REQUIRES_DEVICE_VERIFICATION;
        }
    }
}