package com.dvein.banking_backend.auth.model;

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
@Table(name = "sessions")
@EntityListeners(AuditingEntityListener.class)
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token", nullable = false, unique = true, length = 500)
    private String refreshToken;

    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    // Helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    public void invalidate() {
        this.active = false;
    }
}