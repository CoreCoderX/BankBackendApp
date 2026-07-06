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
@Schema(description = "Raise dispute request")
public class RaiseDisputeRequest {

    @NotBlank(message = "Dispute reason is required")
    @Size(min = 10, max = 500, message = "Dispute reason must be between 10 and 500 characters")
    @Schema(description = "Reason for dispute", example = "Transaction not authorized by me")
    private String disputeReason;
}