package com.dvein.banking_backend.admin.dto.request;

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
@Schema(description = "Admin login request")
public class AdminLoginRequest {

    @NotBlank(message = "Email is required")
    @Schema(description = "Admin email", example = "admin@banking.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "Admin password", example = "Admin@123")
    private String password;
}