package com.dvein.banking_backend.account.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.dvein.banking_backend.common.enums.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Customer profile response")
public class CustomerProfileResponse {

    @Schema(description = "Customer ID", example = "1")
    private Long customerId;

    @Schema(description = "Email", example = "john@example.com")
    private String email;

    @Schema(description = "Phone", example = "9876543210")
    private String phone;

    @Schema(description = "First name")
    private String firstName;

    @Schema(description = "Last name")
    private String lastName;

    @Schema(description = "Full name")
    private String fullName;

    @Schema(description = "Date of birth")
    private String dateOfBirth;

    @Schema(description = "Address")
    private String address;

    @Schema(description = "City")
    private String city;

    @Schema(description = "State")
    private String state;

    @Schema(description = "Postal code")
    private String postalCode;

    @Schema(description = "Country")
    private String country;

    @Schema(description = "PAN")
    private String pan;

    @Schema(description = "Aadhaar")
    private String aadhaar;

    @Schema(description = "Profile photo URL")
    private String profilePhotoUrl;

    @Schema(description = "Customer status")
    private CustomerStatus status;

    @Schema(description = "Created at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}