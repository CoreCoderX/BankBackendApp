package com.dvein.banking_backend.account.dto.request;

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
@Schema(description = "KYC submission request")
public class KycSubmissionRequest {

    @NotBlank(message = "PAN is required")
    @Schema(description = "PAN number", example = "ABCDE1234F")
    private String pan;

    @NotBlank(message = "Aadhaar is required")
    @Schema(description = "Aadhaar number", example = "123456789012")
    private String aadhaar;

    @NotBlank(message = "Address is required")
    @Schema(description = "Address")
    private String address;

    @NotBlank(message = "City is required")
    @Schema(description = "City", example = "Mumbai")
    private String city;

    @NotBlank(message = "State is required")
    @Schema(description = "State", example = "Maharashtra")
    private String state;

    @NotBlank(message = "Postal code is required")
    @Schema(description = "Postal code", example = "400001")
    private String postalCode;
}