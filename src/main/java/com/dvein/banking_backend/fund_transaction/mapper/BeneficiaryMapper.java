package com.dvein.banking_backend.transaction.mapper;

import com.dvein.banking_backend.transaction.dto.response.BeneficiaryResponse;
import com.dvein.banking_backend.transaction.model.Beneficiary;
import org.springframework.stereotype.Component;

@Component
public class BeneficiaryMapper {

    public BeneficiaryResponse toResponse(Beneficiary beneficiary) {
        return BeneficiaryResponse.builder()
                .id(beneficiary.getId())
                .nickname(beneficiary.getNickname())
                .accountNumber(beneficiary.getAccountNumber())
                .ifscCode(beneficiary.getIfscCode())
                .bank_name(beneficiary.getBank_name())
                .branchName(beneficiary.getBranchName())
                .isActive(beneficiary.getIsActive())
                .isVerified(beneficiary.getIsVerified())
                .createdAt(beneficiary.getCreatedAt())
                .build();
    }
}