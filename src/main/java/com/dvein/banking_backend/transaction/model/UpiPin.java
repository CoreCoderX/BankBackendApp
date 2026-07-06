package com.dvein.banking_backend.transaction.model;

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
@Table(name = "upi_pins")
@EntityListeners(AuditingEntityListener.class)
public class UpiPin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "upi_profile_id", nullable = false, unique = true)
    private UpiProfile upiProfile;

    @Column(name = "pin_hash", nullable = false)
    private String pinHash;

    @Column(name = "failed_attempts")
    @Builder.Default
    private int failedAttempts = 0;

    @Column(name = "is_locked")
    @Builder.Default
    private boolean locked = false;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    public void incrementFailedAttempts() {
        this.failedAttempts++;
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.locked = false;
        this.lockedUntil = null;
    }

    public void lock(long durationInMillis) {
        this.locked = true;
        this.lockedUntil = LocalDateTime.now().plusSeconds(durationInMillis / 1000);
    }

    public boolean isPinLocked() {
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