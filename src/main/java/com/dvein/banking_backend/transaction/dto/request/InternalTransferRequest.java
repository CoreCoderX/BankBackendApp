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
@Schema(description = "Internal transfer request (same bank)")
public class InternalTransferRequest {

    @NotNull(message = "Sender account ID is required")
    @Schema(description = "Sender account ID", example = "1")
    private Long senderAccountId;

    @NotBlank(message = "Receiver account number is required")
    @Schema(description = "Receiver account number", example = "ACC20240115002")
    private String receiverAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be at least 1")
    @Schema(description = "Transfer amount", example = "5000.00")
    private BigDecimal amount;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Transfer description", example = "Payment for services")
    private String description;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    @Schema(description = "Additional remarks", example = "Urgent payment")
    private String remarks;

    @NotBlank(message = "Idempotency key is required")
    @Schema(description = "Unique idempotency key to prevent duplicate transactions",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private String idempotencyKey;

    @Pattern(regexp = "^\\d{4}$", message = "MPIN must be 4 digits")
    @Schema(description = "4-digit MPIN for verification", example = "1234")
    private String mpin;
}