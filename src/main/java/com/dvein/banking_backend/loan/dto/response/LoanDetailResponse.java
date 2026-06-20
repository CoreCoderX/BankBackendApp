package com.dvein.banking_backend.loan.dto.response;

import com.dvein.banking_backend.loan.enums.LoanStatus;
import com.dvein.banking_backend.loan.enums.LoanType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDetailResponse {

    private Long id;
    private String loanNumber;
    private LoanType loanType;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal emiAmount;
    private BigDecimal remainingPrincipal;
    private BigDecimal totalInterest;
    private BigDecimal totalPayable;
    private BigDecimal amountPaid;
    private LoanStatus status;
    private LocalDate appliedDate;
    private LocalDate approvedDate;
    private LocalDate disbursedDate;
    private LocalDate closedDate;
    private LocalDate firstEmiDate;
    private String purpose;
    private String remarks;
    private String rejectionReason;
    private String accountNumber;
    private String customerName;
    private LocalDateTime createdAt;
}