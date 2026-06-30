package com.dvein.banking_backend.loan.mapper;

import com.dvein.banking_backend.loan.dto.response.LoanDetailResponse;
import com.dvein.banking_backend.loan.dto.response.LoanResponse;
import com.dvein.banking_backend.loan.model.Loan;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {

    public LoanResponse toResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .loanNumber(loan.getLoanNumber())
                .loanType(loan.getLoanType())
                .principalAmount(loan.getPrincipalAmount())
                .interestRate(loan.getInterestRate())
                .tenureMonths(loan.getTenureMonths())
                .emiAmount(loan.getEmiAmount())
                .remainingPrincipal(loan.getRemainingPrincipal())
                .status(loan.getStatus())
                .appliedDate(loan.getAppliedDate())
                .approvedDate(loan.getApprovedDate())
                .disbursedDate(loan.getDisbursedDate())
                .accountNumber(loan.getAccount() != null ? loan.getAccount().getAccountNumber() : null)
                .build();
    }

    public LoanDetailResponse toDetailResponse(Loan loan) {
        return LoanDetailResponse.builder()
                .id(loan.getId())
                .loanNumber(loan.getLoanNumber())
                .loanType(loan.getLoanType())
                .principalAmount(loan.getPrincipalAmount())
                .interestRate(loan.getInterestRate())
                .tenureMonths(loan.getTenureMonths())
                .emiAmount(loan.getEmiAmount())
                .remainingPrincipal(loan.getRemainingPrincipal())
                .totalInterest(loan.getTotalInterest())
                .totalPayable(loan.getTotalPayable())
                .amountPaid(loan.getAmountPaid())
                .status(loan.getStatus())
                .appliedDate(loan.getAppliedDate())
                .approvedDate(loan.getApprovedDate())
                .disbursedDate(loan.getDisbursedDate())
                .closedDate(loan.getClosedDate())
                .firstEmiDate(loan.getFirstEmiDate())
                .purpose(loan.getPurpose())
                .remarks(loan.getRemarks())
                .rejectionReason(loan.getRejectionReason())
                .accountNumber(loan.getAccount() != null ? loan.getAccount().getAccountNumber() : null)
                .customerName(
                        loan.getAccount() != null && loan.getAccount().getCustomer() != null ?
                                loan.getAccount().getCustomer().getFullName() : null
                )
                .createdAt(loan.getCreatedAt())
                .build();
    }
}