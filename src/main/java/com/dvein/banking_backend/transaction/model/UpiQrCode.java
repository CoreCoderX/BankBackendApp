package com.dvein.banking_backend.transaction.model;

import com.dvein.banking_backend.transaction.enums.QrType;
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
@Table(name = "upi_qr_codes")
@EntityListeners(AuditingEntityListener.class)
public class UpiQrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "qr_id", nullable = false, unique = true, length = 50)
    private String qrId;

    @Column(name = "upi_id", nullable = false, length = 100)
    private String upiId;

    @Enumerated(EnumType.STRING)
    @Column(name = "qr_type", nullable = false, length = 20)
    private QrType qrType;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(length = 500)
    private String description;

    @Column(name = "qr_data", nullable = false, columnDefinition = "TEXT")
    private String qrData;

    @Column(name = "qr_image_base64", columnDefinition = "TEXT")
    private String qrImageBase64;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "scan_count")
    @Builder.Default
    private int scanCount = 0;

    @Column(name = "max_scans")
    private Integer maxScans;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void incrementScanCount() {
        this.scanCount++;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isMaxScansReached() {
        return maxScans != null && scanCount >= maxScans;
    }
}