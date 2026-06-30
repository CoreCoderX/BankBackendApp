package com.dvein.banking_backend.loan.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanClosureRequest {

    @NotNull(message = "Loan ID is required")
    private Long loanId;

    @NotNull(message = "Account ID is required")
    private Long accountId;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
}