package com.dvein.banking_backend.auth.dto.request;

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
@Schema(description = "Verify device request")
public class VerifyDeviceRequest {

    @NotBlank(message = "Verification code is required")
    @Pattern(regexp = "^\\d{6}$", message = "Verification code must be 6 digits")
    @Schema(description = "6-digit verification code", example = "123456")
    private String verificationCode;

    @NotBlank(message = "Pre-auth token is required")
    @Schema(description = "Pre-auth token from initial login")
    private String preAuthToken;

    @Schema(description = "Trust this device for future logins", example = "true")
    @Builder.Default
    private boolean trustDevice = false;
}