package com.dvein.banking_backend.loan.dto.response;

import com.dvein.banking_backend.loan.enums.LoanStatus;
import com.dvein.banking_backend.loan.enums.LoanType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponse {

    private Long id;
    private String loanNumber;
    private LoanType loanType;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal emiAmount;
    private BigDecimal remainingPrincipal;
    private LoanStatus status;
    private LocalDate appliedDate;
    private LocalDate approvedDate;
    private LocalDate disbursedDate;
    private String accountNumber;
}