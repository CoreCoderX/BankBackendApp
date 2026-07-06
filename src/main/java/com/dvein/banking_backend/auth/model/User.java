package com.dvein.banking_backend.auth.model;

import com.dvein.banking_backend.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(unique = true, length = 15)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "is_active", nullable = false)  // Changed from "active"
    @Builder.Default
    private boolean active = false;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "is_locked", nullable = false)  // Changed from "locked"
    @Builder.Default
    private boolean locked = false;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "totp_enabled", nullable = false)
    @Builder.Default
    private boolean totpEnabled = false;

    @Column(name = "biometric_enabled", nullable = false)
    @Builder.Default
    private boolean biometricEnabled = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Helper methods
    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.locked = false;
        this.lockedUntil = null;
    }

    public void lock(long durationInMillis) {
        this.locked = true;
        this.lockedUntil = LocalDateTime.now().plusSeconds(durationInMillis / 1000);
    }

    public boolean isAccountLocked() {
        if (!locked) {
            return false;
        }

        if (lockedUntil != null && LocalDateTime.now().isAfter(lockedUntil)) {
            resetFailedAttempts();
            return false;
        }

        return true;
    }
}