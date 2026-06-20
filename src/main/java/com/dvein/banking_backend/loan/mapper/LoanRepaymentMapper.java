package com.dvein.banking_backend.loan.mapper;

import com.dvein.banking_backend.loan.dto.response.LoanRepaymentResponse;
import com.dvein.banking_backend.loan.model.LoanRepayment;
import org.springframework.stereotype.Component;

@Component
public class LoanRepaymentMapper {

    public LoanRepaymentResponse toResponse(LoanRepayment repayment) {
        return LoanRepaymentResponse.builder()
                .id(repayment.getId())
                .loanNumber(repayment.getLoan().getLoanNumber())
                .paymentAmount(repayment.getPaymentAmount())
                .principalPaid(repayment.getPrincipalPaid())
                .interestPaid(repayment.getInterestPaid())
                .penaltyPaid(repayment.getPenaltyPaid())
                .remainingBalance(repayment.getRemainingBalance())
                .paymentDate(repayment.getPaymentDate())
                .status(repayment.getStatus())
                .transactionId(repayment.getTransactionId())
                .createdAt(repayment.getCreatedAt())
                .build();
    }
}