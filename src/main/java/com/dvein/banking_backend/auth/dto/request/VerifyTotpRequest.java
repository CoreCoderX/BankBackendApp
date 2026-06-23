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
@Schema(description = "Verify TOTP request")
public class VerifyTotpRequest {

    @NotBlank(message = "TOTP code is required")
    @Pattern(regexp = "^\\d{6}$", message = "TOTP code must be 6 digits")
    @Schema(description = "6-digit TOTP code", example = "123456")
    private String code;

    @Schema(description = "Pre-auth token from initial login", example = "pre_auth_token_xyz")
    private String preAuthToken;
}