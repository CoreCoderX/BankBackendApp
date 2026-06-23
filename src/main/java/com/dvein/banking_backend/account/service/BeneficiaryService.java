package com.dvein.banking_backend.account.service;

import com.dvein.banking_backend.account.dto.request.AddBeneficiaryRequest;
import com.dvein.banking_backend.account.dto.response.BeneficiaryResponse;
import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Beneficiary;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.account.repository.BeneficiaryRepository;
import com.dvein.banking_backend.common.exception.DuplicateResourceException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public BeneficiaryResponse addBeneficiary(Long accountId, AddBeneficiaryRequest request, String email) {
        Account account = accountRepository.findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        // Check if beneficiary already exists
        if (beneficiaryRepository.existsByAccountAndBeneficiaryAccountNumber(account, request.getBeneficiaryAccountNumber())) {
            throw new DuplicateResourceException("Beneficiary", "account number");
        }

        Beneficiary beneficiary = Beneficiary.builder()
                .account(account)
                .beneficiaryName(request.getBeneficiaryName())
                .beneficiaryAccountNumber(request.getBeneficiaryAccountNumber())
                .ifscCode(request.getIfscCode())
                .bankName(request.getBankName())
                .remarks(request.getRemarks())
                .build();

        beneficiary = beneficiaryRepository.save(beneficiary);

        log.info("Beneficiary added for account: {}", accountId);

        return mapToBeneficiaryResponse(beneficiary);
    }

    public List<BeneficiaryResponse> getAccountBeneficiaries(Long accountId, String email) {
        Account account = accountRepository.findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        List<Beneficiary> beneficiaries = beneficiaryRepository.findByAccount(account);

        return beneficiaries.stream()
                .map(this::mapToBeneficiaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeBeneficiary(Long accountId, Long beneficiaryId, String email) {
        Account account = accountRepository.findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary", "id", beneficiaryId));

        if (!beneficiary.getAccount().getId().equals(accountId)) {
            throw new ResourceNotFoundException("Beneficiary not found for account");
        }

        beneficiaryRepository.delete(beneficiary);

        log.info("Beneficiary removed: {} from account: {}", beneficiaryId, accountId);
    }

    @Transactional
    public void verifyBeneficiary(Long beneficiaryId) {
        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary", "id", beneficiaryId));

        beneficiary.setVerified(true);
        beneficiaryRepository.save(beneficiary);

        log.info("Beneficiary verified: {}", beneficiaryId);
    }

    private BeneficiaryResponse mapToBeneficiaryResponse(Beneficiary beneficiary) {
        return BeneficiaryResponse.builder()
                .beneficiaryId(beneficiary.getId())
                .beneficiaryName(beneficiary.getBeneficiaryName())
                .beneficiaryAccountNumber(beneficiary.getBeneficiaryAccountNumber())
                .ifscCode(beneficiary.getIfscCode())
                .bankName(beneficiary.getBankName())
                .verified(beneficiary.isVerified())
                .remarks(beneficiary.getRemarks())
                .createdAt(beneficiary.getCreatedAt())
                .build();
    }
}