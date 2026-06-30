package com.dvein.banking_backend.transaction.dto.response;

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
@Schema(description = "Merchant response")
public class MerchantResponse {

    @Schema(description = "Merchant ID", example = "1")
    private Long id;

    @Schema(description = "Merchant code", example = "MER001")
    private String merchantCode;

    @Schema(description = "Merchant name", example = "BigBazaar Supermarket")
    private String merchantName;

    @Schema(description = "Category name", example = "Grocery")
    private String categoryName;

    @Schema(description = "UPI ID", example = "bigbazaar@dveinbank")
    private String upiId;

    @Schema(description = "Verified", example = "true")
    private boolean verified;

    @Schema(description = "Active", example = "true")
    private boolean active;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;
}