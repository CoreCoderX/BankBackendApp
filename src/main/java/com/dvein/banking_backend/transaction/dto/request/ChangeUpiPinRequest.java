package com.dvein.banking_backend.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Change UPI PIN request")
public class ChangeUpiPinRequest {

    @NotBlank(message = "Old PIN is required")
    @Pattern(regexp = "^\\d{6}$", message = "Old PIN must be 6 digits")
    @Schema(description = "Current 6-digit UPI PIN", example = "123456")
    private String oldPin;

    @NotBlank(message = "New PIN is required")
    @Pattern(regexp = "^\\d{6}$", message = "New PIN must be 6 digits")
    @Schema(description = "New 6-digit UPI PIN", example = "654321")
    private String newPin;

    @NotBlank(message = "Confirm PIN is required")
    @Pattern(regexp = "^\\d{6}$", message = "Confirm PIN must be 6 digits")
    @Schema(description = "Confirm new UPI PIN", example = "654321")
    private String confirmPin;
}