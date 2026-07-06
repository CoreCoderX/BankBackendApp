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
@Schema(description = "Schedule payment request")
public class SchedulePaymentRequest {

    @NotNull(message = "Sender account ID is required")
    @Schema(description = "Sender account ID", example = "1")
    private Long senderAccountId;

    @Schema(description = "Receiver account ID (for internal transfers)")
    private Long receiverAccountId;

    @Schema(description = "Beneficiary ID (for beneficiary transfers)")
    private Long beneficiaryId;

    @Schema(description = "Receiver account number (for external transfers)")
    private String receiverAccountNumber;

    @Schema(description = "IFSC code (for external transfers)")
    private String receiverIfscCode;

    @Schema(description = "Receiver name (for external transfers)")
    private String receiverName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be at least 1")
    @Schema(description = "Transfer amount", example = "5000.00")
    private BigDecimal amount;

    @NotBlank(message = "Transaction type is required")
    @Schema(description = "Transaction type", example = "INTERNAL_TRANSFER")
    private String transactionType;

    @NotBlank(message = "Payment method is required")
    @Schema(description = "Payment method", example = "ACCOUNT_TRANSFER")
    private String paymentMethod;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Payment description")
    private String description;

    @NotBlank(message = "Frequency is required")
    @Pattern(regexp = "^(ONE_TIME|DAILY|WEEKLY|MONTHLY|YEARLY)$")
    @Schema(description = "Schedule frequency", example = "MONTHLY")
    private String frequency;

    @NotNull(message = "Start date is required")
    @Schema(description = "Start date", example = "2024-02-01")
    private LocalDate startDate;

    @Schema(description = "End date (optional for recurring)", example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "Execution time", example = "09:00:00")
    private LocalTime executionTime;
}