package com.dvein.banking_backend.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registration response")
public class RegisterResponse {

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Success message")
    private String message;

    @Schema(description = "Whether email verification is required", example = "true")
    @Builder.Default
    private boolean requiresEmailVerification = true;
}