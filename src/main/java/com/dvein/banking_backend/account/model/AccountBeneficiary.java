package com.dvein.banking_backend.account.model;

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
@Table(name = "account_beneficiaries")
@EntityListeners(AuditingEntityListener.class)
public class AccountBeneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "beneficiary_name", nullable = false, length = 100)
    private String beneficiaryName;

    @Column(name = "beneficiary_account_number", nullable = false, length = 20)
    private String beneficiaryAccountNumber;

    @Column(name = "ifsc_code", nullable = false, length = 20)
    private String ifscCode;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean verified = false;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}