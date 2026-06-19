package com.dvein.banking_backend.account.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nominees")
@EntityListeners(AuditingEntityListener.class)
public class Nominee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "nominee_name", nullable = false, length = 100)
    private String nomineeName;

    @Column(name = "nominee_date_of_birth")
    private LocalDate nomineeDateOfBirth;

    @Column(name = "nominee_relationship", length = 100)
    private String nomineeRelationship;

    @Column(name = "nominee_phone", length = 20)
    private String nomineePhone;

    @Column(name = "nominee_email", length = 100)
    private String nomineeEmail;

    @Column(name = "nominee_address", length = 200)
    private String nomineeAddress;

    @Column(name = "percentage", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal percentage = BigDecimal.valueOf(100);

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}