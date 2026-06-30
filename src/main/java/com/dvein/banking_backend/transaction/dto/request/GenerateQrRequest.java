package com.dvein.banking_backend.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generate UPI QR code request")
public class GenerateQrRequest {

    @NotBlank(message = "UPI ID is required")
    @Schema(description = "UPI ID for which QR is generated", example = "john@dveinbank")
    private String upiId;

    @NotBlank(message = "QR type is required")
    @Pattern(regexp = "^(STATIC|DYNAMIC)$", message = "QR type must be STATIC or DYNAMIC")
    @Schema(description = "QR code type", example = "DYNAMIC", allowableValues = {"STATIC", "DYNAMIC"})
    private String qrType;

    @DecimalMin(value = "1.0", message = "Amount must be at least 1")
    @Schema(description = "Fixed amount (required for DYNAMIC QR)", example = "500.00")
    private BigDecimal amount;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "QR description", example = "Payment for Product X")
    private String description;

    @Min(value = 1, message = "Expiry hours must be at least 1")
    @Max(value = 720, message = "Expiry hours cannot exceed 720 (30 days)")
    @Schema(description = "QR expiry in hours (for DYNAMIC QR)", example = "24")
    private Integer expiryHours;

    @Min(value = 1, message = "Max scans must be at least 1")
    @Schema(description = "Maximum number of scans allowed (for DYNAMIC QR)", example = "10")
    private Integer maxScans;
}