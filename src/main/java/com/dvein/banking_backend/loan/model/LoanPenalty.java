package com.dvein.banking_backend.loan.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_penalties", indexes = {
        @Index(name = "idx_penalty_loan", columnList = "loan_id"),
        @Index(name = "idx_penalty_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanPenalty {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "penalty_seq")
    @SequenceGenerator(name = "penalty_seq", sequenceName = "penalty_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 200)
    private String reason;

    @Column(nullable = false)
    private Boolean isPaid = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}