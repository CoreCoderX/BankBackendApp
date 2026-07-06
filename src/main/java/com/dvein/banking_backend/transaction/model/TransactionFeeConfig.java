package com.dvein.banking_backend.transaction.model;

import com.dvein.banking_backend.auth.model.User;
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
@Table(name = "transaction_fee_config")
@EntityListeners(AuditingEntityListener.class)
public class TransactionFeeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_type", nullable = false, unique = true, length = 50)
    private String transactionType;

    @Column(name = "base_fee", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal baseFee = BigDecimal.ZERO;

    @Column(name = "gst_percentage", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal gstPercentage = BigDecimal.valueOf(18);

    @Column(name = "min_amount", precision = 18, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 18, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}