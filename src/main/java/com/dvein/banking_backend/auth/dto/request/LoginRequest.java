package com.dvein.banking_backend.auth.dto.request;

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
@Schema(description = "Login request")
public class LoginRequest {

    @NotBlank(message = "Email or phone is required")
    @Schema(description = "Email or phone number", example = "john.doe@example.com")
    private String identifier;

    @NotBlank(message = "Password is required")
    @Schema(description = "Account password", example = "StrongPass@123")
    private String password;

    @Schema(description = "Device ID for device management", example = "device-uuid-1234")
    private String deviceId;

    @Schema(description = "Device name", example = "Chrome on Windows")
    private String deviceName;
}