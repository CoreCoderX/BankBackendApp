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
@Schema(description = "Refund request")
public class RefundRequest {

    @NotBlank(message = "Refund reason is required")
    @Size(max = 500, message = "Refund reason cannot exceed 500 characters")
    @Schema(description = "Reason for refund", example = "Duplicate transaction")
    private String reason;
}