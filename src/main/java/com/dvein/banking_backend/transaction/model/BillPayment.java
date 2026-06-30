package com.dvein.banking_backend.transaction.model;

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
@Table(name = "bill_payments")
@EntityListeners(AuditingEntityListener.class)
public class BillPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    @ManyToOne
    @JoinColumn(name = "biller_id")
    private Biller biller;

    @Column(name = "bill_category", nullable = false, length = 50)
    private String billCategory;

    @Column(name = "bill_number", length = 100)
    private String billNumber;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "late_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal lateFee = BigDecimal.ZERO;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}