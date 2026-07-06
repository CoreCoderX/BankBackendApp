package com.dvein.banking_backend.transaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed transaction response with metadata")
public class TransactionDetailsResponse {

    @Schema(description = "Transaction basic details")
    private TransactionResponse transaction;

    @Schema(description = "Additional metadata")
    private Map<String, String> metadata;

    @Schema(description = "UPI transaction details (if applicable)")
    private UpiTransactionResponse upiDetails;

    @Schema(description = "Bill payment details (if applicable)")
    private BillPaymentDetailsResponse billDetails;

    @Schema(description = "Merchant payment details (if applicable)")
    private MerchantPaymentDetailsResponse merchantDetails;

    @Schema(description = "Receipt number")
    private String receiptNumber;

    @Schema(description = "Receipt available", example = "true")
    private boolean receiptAvailable;
}