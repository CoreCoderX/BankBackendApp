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
@Schema(description = "Merchant payment request")
public class MerchantPaymentRequest {

    @NotNull(message = "Account ID is required")
    @Schema(description = "Account ID", example = "1")
    private Long accountId;

    @NotBlank(message = "Merchant code is required")
    @Schema(description = "Merchant code", example = "MER001")
    private String merchantCode;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be at least 1")
    @Schema(description = "Payment amount", example = "2500.00")
    private BigDecimal amount;

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "^(UPI|QR_CODE|DEBIT_CARD|CREDIT_CARD)$",
            message = "Payment method must be UPI, QR_CODE, DEBIT_CARD, or CREDIT_CARD")
    @Schema(description = "Payment method", example = "UPI")
    private String paymentMethod;

    @Schema(description = "Merchant reference ID")
    private String merchantReferenceId;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Payment description", example = "Purchase at BigBazaar")
    private String description;

    @NotBlank(message = "Idempotency key is required")
    @Schema(description = "Unique idempotency key", example = "550e8400-e29b-41d4-a716-446655440000")
    private String idempotencyKey;

    @Pattern(regexp = "^\\d{4}$", message = "MPIN must be 4 digits")
    @Schema(description = "4-digit MPIN for verification", example = "1234")
    private String mpin;
}