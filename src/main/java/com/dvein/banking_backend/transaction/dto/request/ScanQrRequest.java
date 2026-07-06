package com.dvein.banking_backend.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Scan UPI QR code request")
public class ScanQrRequest {

    @NotBlank(message = "QR data is required")
    @Schema(description = "QR code data", example = "upi://pay?pa=john@dveinbank&pn=John&am=500")
    private String qrData;

    @NotBlank(message = "Payer UPI ID is required")
    @Schema(description = "Payer UPI ID (your UPI ID)", example = "jane@dveinbank")
    private String payerUpiId;

    @NotBlank(message = "UPI PIN is required")
    @Pattern(regexp = "^\\d{6}$", message = "UPI PIN must be 6 digits")
    @Schema(description = "6-digit UPI PIN", example = "123456")
    private String upiPin;

    @NotBlank(message = "Idempotency key is required")
    @Schema(description = "Unique idempotency key", example = "550e8400-e29b-41d4-a716-446655440000")
    private String idempotencyKey;
}