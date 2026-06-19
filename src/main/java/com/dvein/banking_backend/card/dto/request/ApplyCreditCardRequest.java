package com.dvein.banking_backend.card.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Apply for credit card request")
public class ApplyCreditCardRequest {

    @NotNull(message = "Account ID is required")
    @Schema(description = "Account ID", example = "1")
    private Long accountId;

    @NotNull(message = "Requested credit limit is required")
    @Schema(description = "Requested credit limit", example = "100000")
    private BigDecimal requestedCreditLimit;

    @Schema(description = "Card holder name")
    private String cardHolderName;
}