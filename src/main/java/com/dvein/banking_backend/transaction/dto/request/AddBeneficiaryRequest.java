package com.dvein.banking_backend.transaction.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddBeneficiaryRequest {

    @NotBlank(message = "Nickname is required")
    @Size(min = 2, max = 100, message = "Nickname must be between 2 and 100 characters")
    private String nickname;

    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "\\d{10,18}", message = "Invalid account number format")
    private String accountNumber;

    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format")
    private String ifscCode;

    @NotBlank(message = "Bank name is required")
    @Size(max = 200, message = "Bank name cannot exceed 200 characters")
    private String bankName;

    @Size(max = 200, message = "Branch name cannot exceed 200 characters")
    private String branchName;
}