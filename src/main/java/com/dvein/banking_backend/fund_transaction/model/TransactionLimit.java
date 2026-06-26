package com.dvein.banking_backend.transaction.model;

import com.dvein.banking_backend.auth.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_limits", indexes = {
        @Index(name = "idx_limit_user", columnList = "user_id"),
        @Index(name = "idx_limit_date", columnList = "limit_Date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "limit_seq")
    @SequenceGenerator(name = "limit_seq", sequenceName = "limit_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal perTransactionLimit = new BigDecimal("50000.00");

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal dailyLimit = new BigDecimal("200000.00");

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyLimit = new BigDecimal("1000000.00");

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal dailyUsed = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyUsed = BigDecimal.ZERO;

    @Column(name = "limit_date", nullable = false)
    private LocalDate currentDate = LocalDate.now();

    @Column(nullable = false)
    private Integer currentMonth = LocalDate.now().getMonthValue();

    @Column(nullable = false)
    private Integer currentYear = LocalDate.now().getYear();

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}