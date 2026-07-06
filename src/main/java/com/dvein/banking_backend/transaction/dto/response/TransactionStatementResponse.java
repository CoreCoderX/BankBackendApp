package com.dvein.banking_backend.transaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transaction statement response")
public class TransactionStatementResponse {

    @Schema(description = "Account number")
    private String accountNumber;

    @Schema(description = "Account holder name")
    private String accountHolderName;

    @Schema(description = "Statement period start date")
    private LocalDate startDate;

    @Schema(description = "Statement period end date")
    private LocalDate endDate;

    @Schema(description = "Opening balance", example = "50000.00")
    private BigDecimal openingBalance;

    @Schema(description = "Closing balance", example = "55000.00")
    private BigDecimal closingBalance;

    @Schema(description = "Total credits", example = "25000.00")
    private BigDecimal totalCredits;

    @Schema(description = "Total debits", example = "20000.00")
    private BigDecimal totalDebits;

    @Schema(description = "Transaction count", example = "45")
    private long transactionCount;

    @Schema(description = "Transactions")
    private List<TransactionResponse> transactions;

    @Schema(description = "Generated at")
    private LocalDate generatedAt;
}