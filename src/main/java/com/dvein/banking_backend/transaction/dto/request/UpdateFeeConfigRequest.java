package com.dvein.banking_backend.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
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
@Schema(description = "Update transaction fee configuration request (Admin)")
public class UpdateFeeConfigRequest {

    @NotNull(message = "Base fee is required")
    @DecimalMin(value = "0.0", message = "Base fee must be non-negative")
    @Schema(description = "Base transaction fee", example = "5.00")
    private BigDecimal baseFee;

    @NotNull(message = "GST percentage is required")
    @DecimalMin(value = "0.0", message = "GST percentage must be non-negative")
    @Schema(description = "GST percentage", example = "18.00")
    private BigDecimal gstPercentage;

    @Schema(description = "Minimum transaction amount")
    private BigDecimal minAmount;

    @Schema(description = "Maximum transaction amount")
    private BigDecimal maxAmount;

    @Schema(description = "Active status")
    private Boolean active;
}