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
@Schema(description = "External transfer request (IMPS/NEFT/RTGS)")
public class ExternalTransferRequest {

    @NotNull(message = "Sender account ID is required")
    @Schema(description = "Sender account ID", example = "1")
    private Long senderAccountId;

    @NotBlank(message = "Receiver account number is required")
    @Schema(description = "Receiver account number", example = "123456789012")
    private String receiverAccountNumber;

    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code")
    @Schema(description = "IFSC code", example = "HDFC0001234")
    private String ifscCode;

    @NotBlank(message = "Receiver name is required")
    @Schema(description = "Receiver name", example = "John Doe")
    private String receiverName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be at least 1")
    @Schema(description = "Transfer amount", example = "10000.00")
    private BigDecimal amount;

    @NotBlank(message = "Transfer mode is required")
    @Pattern(regexp = "^(IMPS|NEFT|RTGS)$", message = "Transfer mode must be IMPS, NEFT, or RTGS")
    @Schema(description = "Transfer mode", example = "IMPS", allowableValues = {"IMPS", "NEFT", "RTGS"})
    private String transferMode;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Transfer description", example = "Payment to vendor")
    private String description;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    @Schema(description = "Additional remarks")
    private String remarks;

    @NotBlank(message = "Idempotency key is required")
    @Schema(description = "Unique idempotency key", example = "550e8400-e29b-41d4-a716-446655440000")
    private String idempotencyKey;

    @Pattern(regexp = "^\\d{4}$", message = "MPIN must be 4 digits")
    @Schema(description = "4-digit MPIN for verification", example = "1234")
    private String mpin;
}