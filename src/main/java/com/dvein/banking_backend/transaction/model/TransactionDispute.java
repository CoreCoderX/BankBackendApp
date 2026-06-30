package com.dvein.banking_backend.transaction.model;

import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.transaction.enums.DisputeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transaction_disputes")
@EntityListeners(AuditingEntityListener.class)
public class TransactionDispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "dispute_reason", nullable = false, length = 500)
    private String disputeReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private DisputeStatus status = DisputeStatus.RAISED;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @ManyToOne
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}