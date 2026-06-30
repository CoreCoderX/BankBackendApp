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
@Table(name = "token_blacklist")
@EntityListeners(AuditingEntityListener.class)
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, length = 500, unique = true)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reason", length = 100)
    private String reason;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}