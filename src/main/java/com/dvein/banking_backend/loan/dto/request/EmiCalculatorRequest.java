package com.dvein.banking_backend.loan.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmiCalculatorRequest {

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "10000.00", message = "Minimum amount is ₹10,000")
    private BigDecimal principalAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "1.00", message = "Minimum interest rate is 1%")
    @DecimalMax(value = "30.00", message = "Maximum interest rate is 30%")
    private BigDecimal interestRate;

    @NotNull(message = "Tenure is required")
    @Min(value = 1, message = "Minimum tenure is 1 month")
    @Max(value = 360, message = "Maximum tenure is 360 months")
    private Integer tenureMonths;
}