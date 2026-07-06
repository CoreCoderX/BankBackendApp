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
@Schema(description = "UPI collect money request (money request)")
public class UpiCollectMoneyRequest {

    @NotBlank(message = "Requester UPI ID is required")
    @Schema(description = "Requester UPI ID (your UPI ID)", example = "john@dveinbank")
    private String requesterUpiId;

    @NotBlank(message = "Payer UPI ID is required")
    @Schema(description = "Payer UPI ID (from whom you want money)", example = "jane@dveinbank")
    private String payerUpiId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be at least 1")
    @DecimalMax(value = "100000.0", message = "Amount cannot exceed 1,00,000")
    @Schema(description = "Requested amount", example = "1000.00")
    private BigDecimal amount;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Request description", example = "Payment for groceries")
    private String description;
}