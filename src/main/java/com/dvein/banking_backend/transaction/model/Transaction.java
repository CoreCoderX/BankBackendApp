package com.dvein.banking_backend.transaction.model;

import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.enums.TransactionType;
import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.transaction.enums.TransactionMode;
import com.dvein.banking_backend.transaction.enums.PaymentMethod;
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
@Table(name = "transactions")
@EntityListeners(AuditingEntityListener.class)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 50)
    private String transactionId;

    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    // Accounts
    @ManyToOne
    @JoinColumn(name = "sender_account_id")
    private Account senderAccount;

    @ManyToOne
    @JoinColumn(name = "receiver_account_id")
    private Account receiverAccount;

    // External transfer details
    @Column(name = "receiver_account_number", length = 20)
    private String receiverAccountNumber;

    @Column(name = "receiver_ifsc_code", length = 20)
    private String receiverIfscCode;

    @Column(name = "receiver_name", length = 100)
    private String receiverName;

    @Column(name = "receiver_bank_name", length = 100)
    private String receiverBankName;

    // Transaction details
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_mode", nullable = false, length = 50)
    private TransactionMode transactionMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    // Status & workflow
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.INITIATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private TransactionStatus previousStatus;

    // Category & description
    @ManyToOne
    @JoinColumn(name = "category_id")
    private TransactionCategory category;

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String remarks;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "utr_number", length = 100)
    private String utrNumber;

    // Fees & charges
    @Column(name = "transaction_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal transactionFee = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal gst = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    // Balance snapshots
    @Column(name = "sender_balance_before", precision = 18, scale = 2)
    private BigDecimal senderBalanceBefore;

    @Column(name = "sender_balance_after", precision = 18, scale = 2)
    private BigDecimal senderBalanceAfter;

    @Column(name = "receiver_balance_before", precision = 18, scale = 2)
    private BigDecimal receiverBalanceBefore;

    @Column(name = "receiver_balance_after", precision = 18, scale = 2)
    private BigDecimal receiverBalanceAfter;

    // Timestamps
    @Column(name = "initiated_at", nullable = false)
    @Builder.Default
    private LocalDateTime initiatedAt = LocalDateTime.now();

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Column(name = "processing_at")
    private LocalDateTime processingAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "reversed_at")
    private LocalDateTime reversedAt;

    // Failure & reversal
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "reversal_reason", length = 500)
    private String reversalReason;

    @ManyToOne
    @JoinColumn(name = "reversal_transaction_id")
    private Transaction reversalTransaction;

    // Security & fraud
    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "device_id", length = 255)
    private String deviceId;

    @Column(length = 200)
    private String location;

    @Column(name = "fraud_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal fraudScore = BigDecimal.ZERO;

    @Column(name = "is_flagged")
    @Builder.Default
    private boolean flagged = false;

    // Metadata
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Optimistic locking
    @Version
    @Builder.Default
    private Integer version = 0;
}