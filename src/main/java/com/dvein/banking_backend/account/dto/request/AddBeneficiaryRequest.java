package com.dvein.banking_backend.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Add beneficiary request")
public class AddBeneficiaryRequest {

    @NotBlank(message = "Beneficiary name is required")
    @Schema(description = "Beneficiary name", example = "Jane Doe")
    private String beneficiaryName;

    @NotBlank(message = "Beneficiary account number is required")
    @Schema(description = "Account number", example = "ACC20240115001")
    private String beneficiaryAccountNumber;

    @NotBlank(message = "IFSC code is required")
    @Schema(description = "IFSC code", example = "BANK0001234")
    private String ifscCode;

    @Schema(description = "Bank name", example = "DVein Bank")
    private String bankName;

    @Schema(description = "Remarks", example = "Sister")
    private String remarks;
}