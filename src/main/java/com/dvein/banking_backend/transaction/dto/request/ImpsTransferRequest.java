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
@Schema(description = "IMPS transfer request")
public class ImpsTransferRequest {

    @NotNull(message = "Sender account ID is required")
    @Schema(description = "Sender account ID", example = "1")
    private Long senderAccountId;

    @NotBlank(message = "Receiver account number is required")
    @Schema(description = "Receiver account number", example = "123456789012")
    private String receiverAccountNumber;

    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code")
    @Schema(description = "IFSC code", example = "HDFC0001234")
    private String ifscCode;

    @NotBlank(message = "Receiver name is required")
    @Schema(description = "Receiver name", example = "Rahul Sharma")
    private String receiverName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be at least 1")
    @DecimalMax(value = "200000.0", message = "IMPS maximum amount is ₹2,00,000")
    @Schema(description = "Transfer amount", example = "25000.00")
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