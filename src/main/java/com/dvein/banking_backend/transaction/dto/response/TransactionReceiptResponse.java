package com.dvein.banking_backend.transaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transaction receipt response")
public class TransactionReceiptResponse {

    @Schema(description = "Receipt number", example = "REC20240115123456")
    private String receiptNumber;

    @Schema(description = "Receipt data (HTML/JSON)")
    private String receiptData;

    @Schema(description = "QR code Base64")
    private String qrCode;

    @Schema(description = "Generated at")
    private LocalDateTime generatedAt;

    @Schema(description = "Transaction details")
    private TransactionResponse transaction;
}