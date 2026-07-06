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
@Schema(description = "Enable TOTP request")
public class EnableTotpRequest {

    @NotBlank(message = "TOTP code is required")
    @Schema(description = "6-digit TOTP code from authenticator app", example = "123456")
    private String code;
}