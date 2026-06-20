package com.dvein.banking_backend.loan.dto.request;

import com.dvein.banking_backend.loan.enums.LoanType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyLoanRequest {

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "10000.00", message = "Minimum loan amount is ₹10,000")
    @DecimalMax(value = "10000000.00", message = "Maximum loan amount is ₹1,00,00,000")
    private BigDecimal principalAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "5.00", message = "Minimum interest rate is 5%")
    @DecimalMax(value = "20.00", message = "Maximum interest rate is 20%")
    private BigDecimal interestRate;

    @NotNull(message = "Tenure is required")
    @Min(value = 6, message = "Minimum tenure is 6 months")
    @Max(value = 360, message = "Maximum tenure is 360 months")
    private Integer tenureMonths;

    @NotNull(message = "Account ID is required")
    private Long accountId;

    @Size(max = 500, message = "Purpose cannot exceed 500 characters")
    private String purpose;
}