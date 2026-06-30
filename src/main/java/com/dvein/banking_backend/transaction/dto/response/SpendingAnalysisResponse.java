package com.dvein.banking_backend.transaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Spending analysis response")
public class SpendingAnalysisResponse {

    @Schema(description = "Total spending", example = "50000.00")
    private BigDecimal totalSpending;

    @Schema(description = "Total income", example = "75000.00")
    private BigDecimal totalIncome;

    @Schema(description = "Category-wise spending")
    private Map<String, BigDecimal> categoryWiseSpending;

    @Schema(description = "Top merchants")
    private Map<String, BigDecimal> topMerchants;

    @Schema(description = "Daily average spending", example = "1666.67")
    private BigDecimal dailyAverageSpending;

    @Schema(description = "Highest transaction amount", example = "15000.00")
    private BigDecimal highestTransaction;

    @Schema(description = "Lowest transaction amount", example = "50.00")
    private BigDecimal lowestTransaction;

    @Schema(description = "Most used payment method")
    private String mostUsedPaymentMethod;

    @Schema(description = "UPI spending percentage", example = "35.5")
    private BigDecimal upiSpendingPercentage;

    @Schema(description = "Card spending percentage", example = "40.2")
    private BigDecimal cardSpendingPercentage;
}