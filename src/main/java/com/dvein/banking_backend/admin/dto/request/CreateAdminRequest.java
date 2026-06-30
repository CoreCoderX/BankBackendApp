package com.dvein.banking_backend.admin.dto.request;

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
@Schema(description = "Create admin request")
public class CreateAdminRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Admin email", example = "newadmin@banking.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "Admin password")
    private String password;

    @NotBlank(message = "First name is required")
    @Schema(description = "Admin first name", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Admin last name", example = "Doe")
    private String lastName;

    @Schema(description = "Admin role", example = "ADMIN")
    @Builder.Default
    private String role = "ADMIN";
}