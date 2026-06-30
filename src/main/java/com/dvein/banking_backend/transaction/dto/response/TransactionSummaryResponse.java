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
@Schema(description = "Transaction summary response")
public class TransactionSummaryResponse {

    @Schema(description = "Total transactions", example = "150")
    private long totalTransactions;

    @Schema(description = "Completed transactions", example = "145")
    private long completedTransactions;

    @Schema(description = "Failed transactions", example = "3")
    private long failedTransactions;

    @Schema(description = "Pending transactions", example = "2")
    private long pendingTransactions;

    @Schema(description = "Total sent amount", example = "250000.00")
    private BigDecimal totalSentAmount;

    @Schema(description = "Total received amount", example = "150000.00")
    private BigDecimal totalReceivedAmount;

    @Schema(description = "Total fees paid", example = "500.00")
    private BigDecimal totalFeesPaid;

    @Schema(description = "Current balance", example = "75000.00")
    private BigDecimal currentBalance;
}