package com.dvein.banking_backend.loan.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepaymentRequest {

    @NotNull(message = "Loan ID is required")
    private Long loanId;

    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "1.00", message = "Payment amount must be at least ₹1")
    private BigDecimal paymentAmount;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
}