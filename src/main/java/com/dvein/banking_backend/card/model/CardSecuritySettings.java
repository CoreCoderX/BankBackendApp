package com.dvein.banking_backend.card.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "card_security_settings")
@EntityListeners(AuditingEntityListener.class)
public class CardSecuritySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_id", nullable = false, unique = true)
    private Long cardId;

    @Column(name = "card_type", nullable = false, length = 20)
    private String cardType;

    @Column(name = "international_transaction_allowed", nullable = false)
    @Builder.Default
    private boolean internationalTransactionAllowed = false;

    @Column(name = "online_transaction_allowed", nullable = false)
    @Builder.Default
    private boolean onlineTransactionAllowed = true;

    @Column(name = "atm_withdrawal_allowed", nullable = false)
    @Builder.Default
    private boolean atmWithdrawalAllowed = true;

    @Column(name = "contactless_payment_allowed", nullable = false)
    @Builder.Default
    private boolean contactlessPaymentAllowed = true;

    @Column(name = "daily_withdrawal_limit", precision = 15, scale = 2)
    private BigDecimal dailyWithdrawalLimit;

    @Column(name = "daily_transaction_limit", precision = 15, scale = 2)
    private BigDecimal dailyTransactionLimit;

    @Column(name = "monthly_transaction_limit", precision = 15, scale = 2)
    private BigDecimal monthlyTransactionLimit;

    @Column(name = "allowed_countries", length = 200)
    private String allowedCountries;

    @Column(name = "blocked_countries", length = 200)
    private String blockedCountries;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}