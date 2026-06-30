package com.dvein.banking_backend.account.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.dvein.banking_backend.common.enums.AccountStatus;
import com.dvein.banking_backend.common.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account response")
public class AccountResponse {

    @Schema(description = "Account ID", example = "1")
    private Long accountId;

    @Schema(description = "Account number", example = "ACC20240115001")
    private String accountNumber;

    @Schema(description = "Account type")
    private AccountType accountType;

    @Schema(description = "IFSC code", example = "BANK0001234")
    private String ifscCode;

    @Schema(description = "Branch code", example = "001234")
    private String branchCode;

    @Schema(description = "Branch name")
    private String branchName;

    @Schema(description = "Balance")
    private BigDecimal balance;

    @Schema(description = "Minimum balance")
    private BigDecimal minimumBalance;

    @Schema(description = "Account status")
    private AccountStatus status;

    @Schema(description = "Primary account")
    private boolean primary;

    @Schema(description = "Created at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}