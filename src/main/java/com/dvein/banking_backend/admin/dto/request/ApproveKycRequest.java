package com.dvein.banking_backend.admin.dto.request;

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
@Schema(description = "Approve KYC request")
public class ApproveKycRequest {

    @NotBlank(message = "Approved by is required")
    @Schema(description = "Admin who approved", example = "admin@banking.com")
    private String approvedBy;

    @Schema(description = "Approval notes", example = "All documents verified")
    private String notes;
}