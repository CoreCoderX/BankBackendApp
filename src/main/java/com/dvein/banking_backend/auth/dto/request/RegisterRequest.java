package com.dvein.banking_backend.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Customer registration request")
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Customer email address", example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    @Schema(description = "Customer phone number", example = "9876543210")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Schema(description = "Account password", example = "StrongPass@123")
    private String password;

    @NotBlank(message = "First name is required")
    @Schema(description = "Customer first name", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Customer last name", example = "Doe")
    private String lastName;

    @Schema(description = "Date of birth", example = "1990-01-15")
    private String dateOfBirth;
}