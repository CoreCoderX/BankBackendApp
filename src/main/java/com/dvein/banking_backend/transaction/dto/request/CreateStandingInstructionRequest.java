package com.dvein.banking_backend.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create standing instruction request")
public class CreateStandingInstructionRequest {

    @NotNull(message = "Sender account ID is required")
    @Schema(description = "Sender account ID", example = "1")
    private Long senderAccountId;

    @Schema(description = "Receiver account ID")
    private Long receiverAccountId;

    @Schema(description = "Beneficiary ID")
    private Long beneficiaryId;

    @Schema(description = "Receiver account number")
    private String receiverAccountNumber;

    @Schema(description = "IFSC code")
    private String receiverIfscCode;

    @Schema(description = "Receiver name")
    private String receiverName;

    @NotNull(message = "Max amount is required")
    @DecimalMin(value = "1.0", message = "Max amount must be at least 1")
    @DecimalMax(value = "50000.0", message = "Max amount cannot exceed 50,000")
    @Schema(description = "Maximum amount per execution", example = "10000.00")
    private BigDecimal maxAmount;

    @NotBlank(message = "Transaction type is required")
    @Schema(description = "Transaction type", example = "INTERNAL_TRANSFER")
    private String transactionType;

    @NotBlank(message = "Payment method is required")
    @Schema(description = "Payment method", example = "ACCOUNT_TRANSFER")
    private String paymentMethod;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "SI description")
    private String description;

    @NotBlank(message = "Frequency is required")
    @Pattern(regexp = "^(DAILY|WEEKLY|MONTHLY|YEARLY)$")
    @Schema(description = "Execution frequency", example = "MONTHLY")
    private String frequency;

    @NotNull(message = "Start date is required")
    @Schema(description = "Start date", example = "2024-02-01")
    private LocalDate startDate;

    @Schema(description = "End date", example = "2025-01-31")
    private LocalDate endDate;

    @Schema(description = "Execution time", example = "09:00:00")
    private LocalTime executionTime;
}