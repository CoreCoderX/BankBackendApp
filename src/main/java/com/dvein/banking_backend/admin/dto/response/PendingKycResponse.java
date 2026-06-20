package com.dvein.banking_backend.admin.dto.response;

import com.dvein.banking_backend.account.dto.response.CustomerProfileResponse;
import com.dvein.banking_backend.account.dto.response.KycStatusResponse;
import com.dvein.banking_backend.account.dto.response.DocumentResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pending KYC response")
public class PendingKycResponse {

    @Schema(description = "Customer profile")
    private CustomerProfileResponse customer;

    @Schema(description = "KYC status")
    private KycStatusResponse kycStatus;

    @Schema(description = "Submitted documents")
    private List<DocumentResponse> documents;
}