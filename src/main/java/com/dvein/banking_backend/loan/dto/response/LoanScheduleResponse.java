package com.dvein.banking_backend.loan.dto.response;

import com.dvein.banking_backend.loan.enums.RepaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanScheduleResponse {

    private Integer emiNumber;
    private LocalDate dueDate;
    private BigDecimal emiAmount;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private BigDecimal outstandingPrincipal;
    private RepaymentStatus status;
    private LocalDate paidDate;
}