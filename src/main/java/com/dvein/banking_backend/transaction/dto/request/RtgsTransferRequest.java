package com.dvein.banking_backend.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "RTGS transfer request")
public class RtgsTransferRequest {

    @NotNull(message = "Sender account ID is required")
    @Schema(description = "Sender account ID", example = "1")
    private Long senderAccountId;

    @NotBlank(message = "Receiver account number is required")
    @Schema(description = "Receiver account number")
    private String receiverAccountNumber;

    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code")
    @Schema(description = "IFSC code", example = "BARB0001234")
    private String ifscCode;

    @NotBlank(message = "Receiver name is required")
    @Schema(description = "Receiver name")
    private String receiverName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "200000.0", message = "RTGS minimum amount is ₹2,00,000")
    @Schema(description = "Transfer amount (minimum ₹2,00,000)", example = "500000.00")
    private BigDecimal amount;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    @Schema(description = "Transfer remarks")
    private String remarks;

    @NotBlank(message = "Idempotency key is required")
    @Schema(description = "Unique idempotency key")
    private String idempotencyKey;

    @Pattern(regexp = "^\\d{4}$", message = "MPIN must be 4 digits")
    @Schema(description = "4-digit MPIN", example = "1234")
    private String mpin;
}