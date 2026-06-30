package com.dvein.banking_backend.transaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transaction limit response")
public class TransactionLimitResponse {

    @Schema(description = "Per transaction limit", example = "50000.00")
    private BigDecimal perTransactionLimit;

    @Schema(description = "Daily UPI limit", example = "100000.00")
    private BigDecimal dailyUpiLimit;

    @Schema(description = "Daily IMPS limit", example = "200000.00")
    private BigDecimal dailyImpsLimit;

    @Schema(description = "Daily NEFT limit", example = "1000000.00")
    private BigDecimal dailyNeftLimit;

    @Schema(description = "Daily RTGS limit", example = "5000000.00")
    private BigDecimal dailyRtgsLimit;

    @Schema(description = "Daily QR limit", example = "100000.00")
    private BigDecimal dailyQrLimit;

    @Schema(description = "Monthly transfer limit", example = "10000000.00")
    private BigDecimal monthlyTransferLimit;

    @Schema(description = "Daily UPI used", example = "25000.00")
    private BigDecimal dailyUpiUsed;

    @Schema(description = "Daily IMPS used", example = "50000.00")
    private BigDecimal dailyImpsUsed;

    @Schema(description = "Daily NEFT used", example = "100000.00")
    private BigDecimal dailyNeftUsed;

    @Schema(description = "Daily RTGS used", example = "0.00")
    private BigDecimal dailyRtgsUsed;

    @Schema(description = "Daily QR used", example = "5000.00")
    private BigDecimal dailyQrUsed;

    @Schema(description = "Monthly used", example = "500000.00")
    private BigDecimal monthlyUsed;

    @Schema(description = "Last reset date")
    private LocalDate lastResetDate;

    @Schema(description = "Daily UPI available", example = "75000.00")
    private BigDecimal dailyUpiAvailable;

    @Schema(description = "Daily IMPS available", example = "150000.00")
    private BigDecimal dailyImpsAvailable;

    @Schema(description = "Daily NEFT available", example = "900000.00")
    private BigDecimal dailyNeftAvailable;

    @Schema(description = "Daily RTGS available", example = "5000000.00")
    private BigDecimal dailyRtgsAvailable;

    @Schema(description = "Monthly available", example = "9500000.00")
    private BigDecimal monthlyAvailable;
}