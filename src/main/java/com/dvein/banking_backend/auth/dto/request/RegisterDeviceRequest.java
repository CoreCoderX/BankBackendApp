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
@Schema(description = "Register device request")
public class RegisterDeviceRequest {

    @NotBlank(message = "Device ID is required")
    @Schema(description = "Unique device identifier", example = "device-uuid-1234")
    private String deviceId;

    @NotBlank(message = "Device name is required")
    @Schema(description = "Device name", example = "Chrome on Windows")
    private String deviceName;

    @Schema(description = "Trust this device", example = "true")
    @Builder.Default
    private Boolean trusted = false;
}