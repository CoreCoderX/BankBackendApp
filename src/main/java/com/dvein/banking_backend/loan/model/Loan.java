package com.dvein.banking_backend.loan.model;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.loan.enums.LoanStatus;
import com.dvein.banking_backend.loan.enums.LoanType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans", indexes = {
        @Index(name = "idx_loan_number", columnList = "loanNumber"),
        @Index(name = "idx_loan_user", columnList = "user_id"),
        @Index(name = "idx_loan_status", columnList = "status"),
        @Index(name = "idx_loan_applied_date", columnList = "appliedDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loan_seq")
    @SequenceGenerator(name = "loan_seq", sequenceName = "loan_sequence", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String loanNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoanType loanType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(nullable = false)
    private Integer tenureMonths;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal emiAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingPrincipal;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalInterest;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalPayable;

    @Column(precision = 15, scale = 2)
    private BigDecimal amountPaid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanStatus status;

    @Column(nullable = false)
    private LocalDate appliedDate;

    private LocalDate approvedDate;

    private LocalDate disbursedDate;

    private LocalDate closedDate;

    private LocalDate firstEmiDate;

    @Column(length = 500)
    private String purpose;

    @Column(length = 500)
    private String remarks;

    @Column(length = 500)
    private String rejectionReason;

    @Column(length = 100)
    private String approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(length = 100)
    private String createdBy;

    @Column(length = 100)
    private String updatedBy;

    @PrePersist
    public void generateLoanNumber() {
        if (this.loanNumber == null) {
            this.loanNumber = "LN" + System.currentTimeMillis();
        }
        if (this.amountPaid == null) {
            this.amountPaid = BigDecimal.ZERO;
        }
    }
}