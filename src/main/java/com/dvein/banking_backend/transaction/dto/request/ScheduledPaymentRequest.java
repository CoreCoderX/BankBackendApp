package com.dvein.banking_backend.transaction.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledPaymentRequest {

    @NotNull(message = "Beneficiary ID is required")
    private Long beneficiaryId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least ₹1")
    @DecimalMax(value = "50000.00", message = "Amount cannot exceed ₹50,000")
    private BigDecimal amount;

    @NotBlank(message = "Frequency is required")
    @Pattern(regexp = "DAILY|WEEKLY|MONTHLY", message = "Frequency must be DAILY, WEEKLY, or MONTHLY")
    private String frequency;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;
}