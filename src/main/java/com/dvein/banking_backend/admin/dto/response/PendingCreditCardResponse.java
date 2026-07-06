package com.dvein.banking_backend.admin.dto.response;

import com.dvein.banking_backend.account.dto.response.CustomerProfileResponse;
import com.dvein.banking_backend.card.dto.response.CreditCardResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pending credit card response")
public class PendingCreditCardResponse {

    @Schema(description = "Customer profile")
    private CustomerProfileResponse customer;

    @Schema(description = "Credit card application")
    private CreditCardResponse creditCard;

    @Schema(description = "Credit score", example = "750")
    private Integer creditScore;

    @Schema(description = "Monthly income", example = "50000")
    private Double monthlyIncome;
}