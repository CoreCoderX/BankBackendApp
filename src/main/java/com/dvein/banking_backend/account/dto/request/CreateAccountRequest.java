package com.dvein.banking_backend.account.dto.request;

import com.dvein.banking_backend.common.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create account request")
public class CreateAccountRequest {

    @NotNull(message = "Account type is required")
    @Schema(description = "Account type", example = "SAVINGS")
    private AccountType accountType;

    @Schema(description = "Initial deposit amount")
    private Double initialDeposit;
}