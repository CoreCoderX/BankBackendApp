package com.dvein.banking_backend.account.dto.response;

import com.dvein.banking_backend.common.enums.AccountStatus;
import com.dvein.banking_backend.common.enums.CustomerStatus;
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
@Schema(description = "Account verification response")
public class AccountVerificationResponse {

    @Schema(description = "Account exists")
    private boolean exists;

    @Schema(description = "Account number")
    private String accountNumber;

    @Schema(description = "Account holder name")
    private String accountHolderName;

    @Schema(description = "Account status")
    private AccountStatus accountStatus;

    @Schema(description = "Customer status")
    private CustomerStatus customerStatus;

    @Schema(description = "KYC status")
    private String kycStatus;

    @Schema(description = "Available balance")
    private BigDecimal availableBalance;

    @Schema(description = "Customer ID")
    private Long customerId;
}