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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "debit_cards")
@EntityListeners(AuditingEntityListener.class)
public class DebitCard {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CardStatus status = CardStatus.INACTIVE;

    @Column(name = "international_enabled", nullable = false)
    @Builder.Default
    private boolean internationalEnabled = false;

    @Column(name = "online_transaction_enabled", nullable = false)
    @Builder.Default
    private boolean onlineTransactionEnabled = true;

    @Column(name = "atm_withdrawal_enabled", nullable = false)
    @Builder.Default
    private boolean atmWithdrawalEnabled = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @Column(name = "block_reason", length = 200)
    private String blockReason;

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }
}