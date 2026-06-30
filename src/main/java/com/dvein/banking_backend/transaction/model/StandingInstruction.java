package com.dvein.banking_backend.transaction.model;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Beneficiary;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.transaction.enums.ExecutionStatus;
import com.dvein.banking_backend.transaction.enums.PaymentMethod;
import com.dvein.banking_backend.transaction.enums.ScheduleFrequency;
import com.dvein.banking_backend.transaction.enums.TransactionType;
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
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "standing_instructions")
@EntityListeners(AuditingEntityListener.class)
public class StandingInstruction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "sender_account_id", nullable = false)
    private Account senderAccount;

    @ManyToOne
    @JoinColumn(name = "receiver_account_id")
    private Account receiverAccount;

    @ManyToOne
    @JoinColumn(name = "beneficiary_id")
    private Beneficiary beneficiary;

    // External transfer
    @Column(name = "receiver_account_number", length = 20)
    private String receiverAccountNumber;

    @Column(name = "receiver_ifsc_code", length = 20)
    private String receiverIfscCode;

    @Column(name = "receiver_name", length = 100)
    private String receiverName;

    // SI details
    @Column(name = "max_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal maxAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    @Column(length = 500)
    private String description;

    // Schedule
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ScheduleFrequency frequency;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "next_execution_date", nullable = false)
    private LocalDate nextExecutionDate;

    @Column(name = "execution_time")
    @Builder.Default
    private LocalTime executionTime = LocalTime.of(9, 0);

    // Status
    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "is_paused")
    @Builder.Default
    private boolean paused = false;

    // Execution tracking
    @Column(name = "total_executions")
    @Builder.Default
    private int totalExecutions = 0;

    @Column(name = "successful_executions")
    @Builder.Default
    private int successfulExecutions = 0;

    @Column(name = "failed_executions")
    @Builder.Default
    private int failedExecutions = 0;

    @Column(name = "last_executed_at")
    private LocalDateTime lastExecutedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_execution_status", length = 30)
    private ExecutionStatus lastExecutionStatus;

    @Column(name = "last_failure_reason", length = 500)
    private String lastFailureReason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}