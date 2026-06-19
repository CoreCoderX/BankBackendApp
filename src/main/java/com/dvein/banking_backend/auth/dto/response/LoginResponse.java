package com.dvein.banking_backend.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Login response")
public class LoginResponse {

    @Schema(description = "Access token (JWT)")
    private String accessToken;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User role", example = "CUSTOMER")
    private String role;

    @Schema(description = "Whether TOTP verification is required", example = "false")
    @Builder.Default
    private boolean requiresTotpVerification = false;

    @Schema(description = "Whether device verification is required", example = "false")
    @Builder.Default
    private boolean requiresDeviceVerification = false;

    @Schema(description = "Session ID", example = "1")
    private Long sessionId;
}