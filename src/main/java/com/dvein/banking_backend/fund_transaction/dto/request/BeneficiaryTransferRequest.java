package com.dvein.banking_backend.transaction.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryTransferRequest {

    @NotNull(message = "Sender account ID is required")
    private Long senderAccountId;

    @NotNull(message = "Beneficiary ID is required")
    private Long beneficiaryId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least ₹1")
    @DecimalMax(value = "50000.00", message = "Amount cannot exceed ₹50,000 per transaction")
    private BigDecimal amount;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
}