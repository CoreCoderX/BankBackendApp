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
@Table(name = "login_history")
@EntityListeners(AuditingEntityListener.class)
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 100)
    private String location;

    @Column(length = 50)
    private String deviceType;

    @Column(nullable = false)
    @Builder.Default
    private boolean successful = true;

    @Column(length = 200)
    private String failureReason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}