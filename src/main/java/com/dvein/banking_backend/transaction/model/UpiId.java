package com.dvein.banking_backend.transaction.model;

import com.dvein.banking_backend.account.model.Account;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "upi_ids")
@EntityListeners(AuditingEntityListener.class)
public class UpiId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "upi_profile_id", nullable = false)
    private UpiProfile upiProfile;

    @Column(name = "upi_id", nullable = false, unique = true, length = 100)
    private String upiId;

    @ManyToOne
    @JoinColumn(name = "linked_account_id")
    private Account linkedAccount;

    @Column(name = "is_primary")
    @Builder.Default
    private boolean primary = false;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}