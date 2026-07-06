package com.dvein.banking_backend.transaction.model;

import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.transaction.enums.FraudRiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fraud_detection_logs")
@EntityListeners(AuditingEntityListener.class)
public class FraudDetectionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "rule_triggered", nullable = false, length = 100)
    private String ruleTriggered;

    @Column(name = "risk_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private FraudRiskLevel riskLevel;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "action_taken", length = 100)
    private String actionTaken;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}