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
@Schema(description = "Reject KYC request")
public class RejectKycRequest {

    @NotBlank(message = "Rejection reason is required")
    @Schema(description = "Reason for rejection", example = "Documents not clear")
    private String reason;

    @Schema(description = "Additional notes", example = "Please resubmit with clear copies")
    private String notes;
}