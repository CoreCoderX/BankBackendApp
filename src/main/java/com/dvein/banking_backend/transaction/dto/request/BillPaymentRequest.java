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
@Schema(description = "Bill payment request")
public class BillPaymentRequest {

    @NotNull(message = "Account ID is required")
    @Schema(description = "Account ID from which payment will be made", example = "1")
    private Long accountId;

    @Schema(description = "Saved biller ID (optional if paying saved biller)")
    private Long billerId;

    @NotBlank(message = "Bill category is required")
    @Schema(description = "Bill category", example = "ELECTRICITY")
    private String billCategory;

    @NotBlank(message = "Biller name is required")
    @Schema(description = "Biller name", example = "Tata Power")
    private String billerName;

    @NotBlank(message = "Bill account number is required")
    @Schema(description = "Bill account number", example = "123456789")
    private String billAccountNumber;

    @Schema(description = "Bill number")
    private String billNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be at least 1")
    @Schema(description = "Bill amount", example = "1500.00")
    private BigDecimal amount;

    @Schema(description = "Late fee (if any)", example = "50.00")
    private BigDecimal lateFee;

    @Schema(description = "Save this biller for future payments", example = "true")
    private Boolean saveBiller;

    @Size(max = 100, message = "Nickname cannot exceed 100 characters")
    @Schema(description = "Nickname for saved biller", example = "Home Electricity")
    private String billerNickname;

    @NotBlank(message = "Idempotency key is required")
    @Schema(description = "Unique idempotency key", example = "550e8400-e29b-41d4-a716-446655440000")
    private String idempotencyKey;

    @Pattern(regexp = "^\\d{4}$", message = "MPIN must be 4 digits")
    @Schema(description = "4-digit MPIN for verification", example = "1234")
    private String mpin;
}