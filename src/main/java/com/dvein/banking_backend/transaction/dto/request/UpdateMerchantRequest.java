package com.dvein.banking_backend.transaction.dto.request;

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
@Schema(description = "Update merchant request (Admin)")
public class UpdateMerchantRequest {

    @NotBlank(message = "Merchant name is required")
    @Schema(description = "Merchant name", example = "BigBazaar Supermarket")
    private String merchantName;

    @Schema(description = "Category ID")
    private Long categoryId;

    @Schema(description = "UPI ID")
    private String upiId;

    @Schema(description = "Verified status")
    private Boolean verified;

    @Schema(description = "Active status")
    private Boolean active;
}