package com.dvein.banking_backend.transaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Estimated transaction charges response")
public class EstimatedChargeResponse {

    @Schema(description = "Transaction type", example = "IMPS")
    private String transactionType;

    @Schema(description = "Transaction amount", example = "50000.00")
    private BigDecimal amount;

    @Schema(description = "Base fee", example = "5.00")
    private BigDecimal baseFee;

    @Schema(description = "GST percentage", example = "18.00")
    private BigDecimal gstPercentage;

    @Schema(description = "GST amount", example = "0.90")
    private BigDecimal gstAmount;

    @Schema(description = "Total fee (base + GST)", example = "5.90")
    private BigDecimal totalFee;

    @Schema(description = "Total debit amount (amount + fee)", example = "50005.90")
    private BigDecimal totalDebitAmount;
}