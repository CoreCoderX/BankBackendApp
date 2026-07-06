package com.dvein.banking_backend.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create UPI ID request")
public class CreateUpiIdRequest {

    @NotBlank(message = "UPI handle is required")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "UPI handle can only contain letters, numbers, dots, hyphens, and underscores")
    @Schema(description = "UPI handle (username or phone)", example = "john")
    private String handle;

    @NotNull(message = "Linked account ID is required")
    @Schema(description = "Account ID to link with this UPI ID", example = "1")
    private Long linkedAccountId;

    @Schema(description = "Set as primary UPI ID", example = "true")
    private Boolean setPrimary;
}