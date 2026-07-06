package com.dvein.banking_backend.transaction.model;

import com.dvein.banking_backend.account.model.Customer;
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
@Table(name = "billers")
@EntityListeners(AuditingEntityListener.class)
public class Biller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "biller_name", nullable = false, length = 100)
    private String billerName;

    @Column(name = "biller_category", nullable = false, length = 50)
    private String billerCategory;

    @Column(name = "account_number", nullable = false, length = 100)
    private String accountNumber;

    @Column(length = 100)
    private String nickname;

    @Column(name = "auto_pay_enabled")
    @Builder.Default
    private boolean autoPayEnabled = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}