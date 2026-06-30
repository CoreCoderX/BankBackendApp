package com.dvein.banking_backend.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update transaction limit request")
public class UpdateTransactionLimitRequest {

    @DecimalMin(value = "1000.0", message = "Per transaction limit must be at least 1000")
    @Schema(description = "Per transaction limit", example = "50000.00")
    private BigDecimal perTransactionLimit;

    @DecimalMin(value = "10000.0", message = "Daily UPI limit must be at least 10000")
    @Schema(description = "Daily UPI limit", example = "100000.00")
    private BigDecimal dailyUpiLimit;

    @DecimalMin(value = "10000.0", message = "Daily IMPS limit must be at least 10000")
    @Schema(description = "Daily IMPS limit", example = "200000.00")
    private BigDecimal dailyImpsLimit;

    @DecimalMin(value = "10000.0", message = "Daily NEFT limit must be at least 10000")
    @Schema(description = "Daily NEFT limit", example = "1000000.00")
    private BigDecimal dailyNeftLimit;

    @DecimalMin(value = "200000.0", message = "Daily RTGS limit must be at least 200000")
    @Schema(description = "Daily RTGS limit", example = "5000000.00")
    private BigDecimal dailyRtgsLimit;

    @DecimalMin(value = "10000.0", message = "Daily QR limit must be at least 10000")
    @Schema(description = "Daily QR limit", example = "100000.00")
    private BigDecimal dailyQrLimit;

    @DecimalMin(value = "100000.0", message = "Monthly transfer limit must be at least 100000")
    @Schema(description = "Monthly transfer limit", example = "10000000.00")
    private BigDecimal monthlyTransferLimit;
}