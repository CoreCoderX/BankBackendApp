package com.dvein.banking_backend.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update UPI ID request")
public class UpdateUpiIdRequest {

    @NotNull(message = "Linked account ID is required")
    @Schema(description = "New linked account ID", example = "2")
    private Long linkedAccountId;
}