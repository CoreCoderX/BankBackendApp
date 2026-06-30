package com.dvein.banking_backend.loan.dto.response;

import com.dvein.banking_backend.loan.enums.RepaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepaymentResponse {

    private Long id;
    private String loanNumber;
    private BigDecimal paymentAmount;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal penaltyPaid;
    private BigDecimal remainingBalance;
    private LocalDate paymentDate;
    private RepaymentStatus status;
    private String transactionId;
    private LocalDateTime createdAt;
}