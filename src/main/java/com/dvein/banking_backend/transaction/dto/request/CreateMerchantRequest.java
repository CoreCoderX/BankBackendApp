package com.dvein.banking_backend.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create merchant request (Admin)")
public class CreateMerchantRequest {

    @NotBlank(message = "Merchant code is required")
    @Schema(description = "Unique merchant code", example = "MER101")
    private String merchantCode;

    @NotBlank(message = "Merchant name is required")
    @Schema(description = "Merchant name", example = "New Merchant Store")
    private String merchantName;

    @NotNull(message = "Category ID is required")
    @Schema(description = "Category ID", example = "1")
    private Long categoryId;

    @Schema(description = "UPI ID", example = "merchant@dveinbank")
    private String upiId;
}