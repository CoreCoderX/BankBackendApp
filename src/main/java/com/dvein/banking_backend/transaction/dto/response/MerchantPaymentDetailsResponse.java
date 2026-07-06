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
@Schema(description = "Merchant payment details")
public class MerchantPaymentDetailsResponse {

    @Schema(description = "Merchant name")
    private String merchantName;

    @Schema(description = "Merchant code")
    private String merchantCode;

    @Schema(description = "Merchant reference ID")
    private String merchantReferenceId;

    @Schema(description = "Cashback amount")
    private BigDecimal cashbackAmount;

    @Schema(description = "Reward points", example = "50")
    private int rewardPoints;
}