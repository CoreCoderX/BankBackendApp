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
@Schema(description = "UPI transaction details response")
public class UpiTransactionResponse {

    @Schema(description = "UPI transaction ID", example = "1")
    private Long id;

    @Schema(description = "Sender UPI ID", example = "john@dveinbank")
    private String senderUpiId;

    @Schema(description = "Receiver UPI ID", example = "jane@dveinbank")
    private String receiverUpiId;

    @Schema(description = "VPA verified", example = "true")
    private boolean vpaVerified;

    @Schema(description = "Collect request ID (if payment via collect request)")
    private String collectRequestId;

    @Schema(description = "QR code ID (if payment via QR)")
    private String qrCodeId;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;
}