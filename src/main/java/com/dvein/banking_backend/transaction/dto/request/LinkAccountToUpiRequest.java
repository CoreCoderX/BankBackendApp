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
@Schema(description = "Link bank account to UPI ID request")
public class LinkAccountToUpiRequest {

    @NotBlank(message = "UPI ID is required")
    @Schema(description = "UPI ID to link", example = "john@dveinbank")
    private String upiId;

    @NotNull(message = "Account ID is required")
    @Schema(description = "Account ID to link", example = "1")
    private Long accountId;

    @NotBlank(message = "UPI PIN is required")
    @Pattern(regexp = "^\\d{6}$", message = "UPI PIN must be 6 digits")
    @Schema(description = "UPI PIN for verification", example = "123456")
    private String upiPin;
}