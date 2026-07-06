package com.dvein.banking_backend.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Estimate transaction charges request")
public class EstimateChargeRequest {

    @NotBlank(message = "Transaction type is required")
    @Schema(description = "Transaction type", example = "IMPS",
            allowableValues = {"IMPS", "NEFT", "RTGS", "UPI_TRANSFER", "INTERNAL_TRANSFER"})
    private String transactionType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be at least 1")
    @Schema(description = "Transaction amount", example = "50000.00")
    private BigDecimal amount;
}