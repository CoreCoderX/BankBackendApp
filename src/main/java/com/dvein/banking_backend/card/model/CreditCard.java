package com.dvein.banking_backend.card.model;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.common.enums.CardStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "credit_cards")
@EntityListeners(AuditingEntityListener.class)
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "card_number", nullable = false, unique = true, length = 16)
    private String cardNumber;

    @Column(name = "card_holder_name", nullable = false, length = 100)
    private String cardHolderName;

    @Column(nullable = false, length = 4)
    private String cvv;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "pin", length = 4)
    private String pin;

    @Column(name = "pin_hash", length = 64)
    private String pinHash;

    @Column(name = "credit_limit", nullable = false, precision = 18, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "available_credit", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal availableCredit = BigDecimal.ZERO;

    @Column(name = "outstanding_balance", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    @Column(name = "interest_rate", nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal interestRate = BigDecimal.valueOf(18.5);

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CardStatus status = CardStatus.INACTIVE;

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private boolean approved = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @Column(name = "block_reason", length = 200)
    private String blockReason;

    @Column(name = "rejection_reason", length = 200)
    private String rejectionReason;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "billing_due_date")
    private LocalDate billingDueDate;

    public boolean isExpired() {
        return expiryDate != null &&
                LocalDate.now().isAfter(expiryDate);
    }
}