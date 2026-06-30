package com.dvein.banking_backend.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to resend an OTP to a registered email address")
public class ResendOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Registered email address", example = "john.doe@example.com")
    private String email;
}
