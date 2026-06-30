package com.dvein.banking_backend.transaction.model;

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
@Table(name = "upi_transactions")
@EntityListeners(AuditingEntityListener.class)
public class UpiTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    @Column(name = "sender_upi_id", nullable = false, length = 100)
    private String senderUpiId;

    @Column(name = "receiver_upi_id", nullable = false, length = 100)
    private String receiverUpiId;

    @Column(name = "vpa_verified")
    @Builder.Default
    private boolean vpaVerified = false;

    @ManyToOne
    @JoinColumn(name = "collect_request_id")
    private UpiCollectRequest collectRequest;

    @ManyToOne
    @JoinColumn(name = "qr_code_id")
    private UpiQrCode qrCode;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}