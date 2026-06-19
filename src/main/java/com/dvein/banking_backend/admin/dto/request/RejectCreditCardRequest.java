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
@Schema(description = "Reject credit card request")
public class RejectCreditCardRequest {

    @NotBlank(message = "Rejection reason is required")
    @Schema(description = "Reason for rejection", example = "Insufficient credit score")
    private String reason;

    @Schema(description = "Additional notes")
    private String notes;
}