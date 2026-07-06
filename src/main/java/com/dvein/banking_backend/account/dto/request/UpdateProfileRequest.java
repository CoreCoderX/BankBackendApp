package com.dvein.banking_backend.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Update customer profile request")
public class UpdateProfileRequest {

    @NotBlank(message = "First name is required")
    @Schema(description = "First name", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "Middle name", example = "Kumar")
    private String middleName;

    @Schema(description = "Address", example = "123 Main Street")
    private String address;

    @Schema(description = "City", example = "Mumbai")
    private String city;

    @Schema(description = "State", example = "Maharashtra")
    private String state;

    @Schema(description = "Postal code", example = "400001")
    private String postalCode;

    @Schema(description = "Country", example = "India")
    private String country;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number")
    @Schema(description = "Phone number", example = "9876543210")
    private String phone;
}