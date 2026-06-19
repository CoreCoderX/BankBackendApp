package com.dvein.banking_backend.auth.dto.request;

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
@Schema(description = "Toggle biometric authentication request")
public class BiometricToggleRequest {

    @NotNull(message = "Enable flag is required")
    @Schema(description = "Enable or disable biometric", example = "true")
    private Boolean enable;
}