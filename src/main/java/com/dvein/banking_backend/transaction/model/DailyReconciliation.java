package com.dvein.banking_backend.transaction.model;

import com.dvein.banking_backend.auth.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "daily_reconciliation")
@EntityListeners(AuditingEntityListener.class)
public class DailyReconciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reconciliation_date", nullable = false, unique = true)
    private LocalDate reconciliationDate;

    @Column(name = "total_transactions", nullable = false)
    private Long totalTransactions;

    @Column(name = "total_debits", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalDebits;

    @Column(name = "total_credits", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalCredits;

    @Column(name = "opening_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "closing_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal closingBalance;

    @Column(name = "calculated_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal calculatedBalance;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal discrepancy;

    @Column(name = "is_balanced", nullable = false)
    private boolean balanced;

    @ManyToOne
    @JoinColumn(name = "reconciled_by")
    private User reconciledBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}