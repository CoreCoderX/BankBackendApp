package com.dvein.banking_backend.loan.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApprovalRequest {

    @NotNull(message = "Loan ID is required")
    private Long loanId;

    @NotNull(message = "Approval decision is required")
    private Boolean approved;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    @Size(max = 500, message = "Rejection reason cannot exceed 500 characters")
    private String rejectionReason;
}