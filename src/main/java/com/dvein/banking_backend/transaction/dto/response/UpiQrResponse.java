package com.dvein.banking_backend.transaction.dto.response;

import com.dvein.banking_backend.transaction.enums.QrType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "UPI QR code response")
public class UpiQrResponse {

    @Schema(description = "QR ID", example = "QR20240115123456")
    private String qrId;

    @Schema(description = "UPI ID", example = "john@dveinbank")
    private String upiId;

    @Schema(description = "QR type")
    private QrType qrType;

    @Schema(description = "Amount (for DYNAMIC QR)")
    private BigDecimal amount;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "QR data string")
    private String qrData;

    @Schema(description = "QR image Base64")
    private String qrImageBase64;

    @Schema(description = "Is active", example = "true")
    private boolean active;

    @Schema(description = "Expires at")
    private LocalDateTime expiresAt;

    @Schema(description = "Scan count", example = "5")
    private int scanCount;

    @Schema(description = "Max scans allowed")
    private Integer maxScans;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;
}