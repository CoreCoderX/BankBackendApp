package com.dvein.banking_backend.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Save biller request")
public class SaveBillerRequest {

    @NotBlank(message = "Biller name is required")
    @Schema(description = "Biller name", example = "Tata Power")
    private String billerName;

    @NotBlank(message = "Bill category is required")
    @Schema(description = "Bill category", example = "ELECTRICITY")
    private String billerCategory;

    @NotBlank(message = "Account number is required")
    @Schema(description = "Bill account number", example = "123456789")
    private String accountNumber;

    @Size(max = 100, message = "Nickname cannot exceed 100 characters")
    @Schema(description = "Nickname for this biller", example = "Home Electricity")
    private String nickname;

    @Schema(description = "Enable auto-pay for this biller", example = "false")
    private Boolean autoPayEnabled;
}