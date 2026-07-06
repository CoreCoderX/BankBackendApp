package com.dvein.banking_backend.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Approve credit card request")
public class ApproveCreditCardRequest {

    @NotNull(message = "Approved credit limit is required")
    @Schema(description = "Approved credit limit", example = "100000")
    private BigDecimal approvedCreditLimit;

    @Schema(description = "Approval notes", example = "Based on credit score and income")
    private String notes;

    @NotNull(message = "Interest rate is required")
    @Schema(description = "Interest rate", example = "18.5")
    private BigDecimal interestRate;
}