package com.dvein.banking_backend.loan.model;

import com.dvein.banking_backend.loan.enums.RepaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_repayments", indexes = {
        @Index(name = "idx_repayment_loan", columnList = "loan_id"),
        @Index(name = "idx_repayment_date", columnList = "paymentDate"),
        @Index(name = "idx_repayment_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "repayment_seq")
    @SequenceGenerator(name = "repayment_seq", sequenceName = "repayment_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal paymentAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalPaid;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal interestPaid;

    @Column(precision = 15, scale = 2)
    private BigDecimal penaltyPaid;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingBalance;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RepaymentStatus status;

    @Column(length = 50)
    private String transactionId;

    @Column(length = 500)
    private String remarks;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void init() {
        if (this.penaltyPaid == null) {
            this.penaltyPaid = BigDecimal.ZERO;
        }
    }
}