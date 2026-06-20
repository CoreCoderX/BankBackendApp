package com.dvein.banking_backend.card.dto.request;

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
@Schema(description = "Card security settings request")
public class CardSecuritySettingsRequest {

    @Schema(description = "Allow international transactions", example = "false")
    private Boolean internationalTransactionAllowed;

    @Schema(description = "Allow online transactions", example = "true")
    private Boolean onlineTransactionAllowed;

    @Schema(description = "Allow ATM withdrawals", example = "true")
    private Boolean atmWithdrawalAllowed;

    @Schema(description = "Allow contactless payments", example = "true")
    private Boolean contactlessPaymentAllowed;

    @Schema(description = "Daily withdrawal limit")
    private BigDecimal dailyWithdrawalLimit;

    @Schema(description = "Daily transaction limit")
    private BigDecimal dailyTransactionLimit;

    @Schema(description = "Monthly transaction limit")
    private BigDecimal monthlyTransactionLimit;
}