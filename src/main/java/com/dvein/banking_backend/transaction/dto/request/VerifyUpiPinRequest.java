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
@Schema(description = "Verify UPI PIN request")
public class VerifyUpiPinRequest {

    @NotBlank(message = "UPI PIN is required")
    @Pattern(regexp = "^\\d{6}$", message = "UPI PIN must be 6 digits")
    @Schema(description = "6-digit UPI PIN", example = "123456")
    private String pin;
}