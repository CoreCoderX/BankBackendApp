package com.dvein.banking_backend.account.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Beneficiary response")
public class BeneficiaryResponse {

    @Schema(description = "Beneficiary ID", example = "1")
    private Long beneficiaryId;

    @Schema(description = "Beneficiary name")
    private String beneficiaryName;

    @Schema(description = "Account number")
    private String beneficiaryAccountNumber;

    @Schema(description = "IFSC code")
    private String ifscCode;

    @Schema(description = "Bank name")
    private String bankName;

    @Schema(description = "Verified")
    private boolean verified;

    @Schema(description = "Remarks")
    private String remarks;

    @Schema(description = "Created at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}