package com.dvein.banking_backend.transaction.model;

import com.dvein.banking_backend.account.model.Customer;
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
@Table(name = "transaction_limits")
@EntityListeners(AuditingEntityListener.class)
public class TransactionLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    // Per transaction limits
    @Column(name = "per_transaction_limit", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal perTransactionLimit = BigDecimal.valueOf(50000);

    // Daily limits
    @Column(name = "daily_upi_limit", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal dailyUpiLimit = BigDecimal.valueOf(100000);

    @Column(name = "daily_imps_limit", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal dailyImpsLimit = BigDecimal.valueOf(200000);

    @Column(name = "daily_neft_limit", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal dailyNeftLimit = BigDecimal.valueOf(1000000);

    @Column(name = "daily_rtgs_limit", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal dailyRtgsLimit = BigDecimal.valueOf(5000000);

    @Column(name = "daily_qr_limit", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal dailyQrLimit = BigDecimal.valueOf(100000);

    // Monthly limits
    @Column(name = "monthly_transfer_limit", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal monthlyTransferLimit = BigDecimal.valueOf(10000000);

    // Tracking (resets daily)
    @Column(name = "daily_upi_used", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal dailyUpiUsed = BigDecimal.ZERO;

    @Column(name = "daily_imps_used", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal dailyImpsUsed = BigDecimal.ZERO;

    @Column(name = "daily_neft_used", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal dailyNeftUsed = BigDecimal.ZERO;

    @Column(name = "daily_rtgs_used", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal dailyRtgsUsed = BigDecimal.ZERO;

    @Column(name = "daily_qr_used", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal dailyQrUsed = BigDecimal.ZERO;

    @Column(name = "monthly_used", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal monthlyUsed = BigDecimal.ZERO;

    @Column(name = "last_reset_date")
    @Builder.Default
    private LocalDate lastResetDate = LocalDate.now();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}