package com.dvein.banking_backend.transaction.model;

import com.dvein.banking_backend.auth.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "beneficiaries",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "accountNumber"}
        ),
        indexes = {
                @Index(name = "idx_beneficiary_user", columnList = "user_id"),
                @Index(name = "idx_beneficiary_account", columnList = "accountNumber")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "beneficiary_seq")
    @SequenceGenerator(name = "beneficiary_seq", sequenceName = "beneficiary_sequence", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(nullable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false, length = 11)
    private String ifscCode;

    @Column(nullable = false, length = 200)
    private String bankName;

    @Column(length = 200)
    private String branchName;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}