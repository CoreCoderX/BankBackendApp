package com.dvein.banking_backend.auth.model;

import com.dvein.banking_backend.common.enums.OtpType;
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
@Table(name = "otps", indexes = {
        @Index(name = "idx_email_type", columnList = "email, otpType")
})
@EntityListeners(AuditingEntityListener.class)
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OtpType otpType;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    @Column(nullable = false)
    @Builder.Default
    private int retryCount = 0;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime verifiedAt;

    // Helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public boolean isMaxRetryReached(int maxRetry) {
        return this.retryCount >= maxRetry;
    }

    public void markAsVerified() {
        this.verified = true;
        this.verifiedAt = LocalDateTime.now();
    }
}