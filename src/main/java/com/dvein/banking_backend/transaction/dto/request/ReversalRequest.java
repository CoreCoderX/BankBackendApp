package com.dvein.banking_backend.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transaction reversal request (Admin)")
public class ReversalRequest {

    @NotBlank(message = "Reversal reason is required")
    @Size(max = 500, message = "Reversal reason cannot exceed 500 characters")
    @Schema(description = "Reason for reversal", example = "Transaction error")
    private String reason;
}