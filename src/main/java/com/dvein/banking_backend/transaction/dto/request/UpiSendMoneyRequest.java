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
@Schema(description = "UPI send money request")
public class UpiSendMoneyRequest {

    @NotBlank(message = "Sender UPI ID is required")
    @Schema(description = "Sender UPI ID", example = "john@dveinbank")
    private String senderUpiId;

    @NotBlank(message = "Receiver UPI ID is required")
    @Schema(description = "Receiver UPI ID", example = "jane@dveinbank")
    private String receiverUpiId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be at least 1")
    @DecimalMax(value = "100000.0", message = "Amount cannot exceed 1,00,000")
    @Schema(description = "Transfer amount", example = "500.00")
    private BigDecimal amount;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Payment description", example = "Lunch payment")
    private String description;

    @NotBlank(message = "UPI PIN is required")
    @Pattern(regexp = "^\\d{6}$", message = "UPI PIN must be 6 digits")
    @Schema(description = "6-digit UPI PIN", example = "123456")
    private String upiPin;

    @NotBlank(message = "Idempotency key is required")
    @Schema(description = "Unique idempotency key", example = "550e8400-e29b-41d4-a716-446655440000")
    private String idempotencyKey;
}