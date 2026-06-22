package com.dvein.banking_backend.account.service;

import com.dvein.banking_backend.account.dto.request.AddBeneficiaryRequest;
import com.dvein.banking_backend.account.dto.response.BeneficiaryResponse;
import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.AccountBeneficiary;
import com.dvein.banking_backend.account.repository.BeneficiaryRepository;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.common.exception.DuplicateResourceException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@RestController
public class AccountBeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public BeneficiaryResponse addBeneficiary(Long accountId, AddBeneficiaryRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        // Check if beneficiary already exists
        if (beneficiaryRepository.existsByAccountAndBeneficiaryAccountNumber(account, request.getBeneficiaryAccountNumber())) {
            throw new DuplicateResourceException("Beneficiary", "account number");
        }

        AccountBeneficiary accountBeneficiary = AccountBeneficiary.builder()
                .account(account)
                .beneficiaryName(request.getBeneficiaryName())
                .beneficiaryAccountNumber(request.getBeneficiaryAccountNumber())
                .ifscCode(request.getIfscCode())
                .bankName(request.getBankName())
                .remarks(request.getRemarks())
                .build();

        accountBeneficiary = beneficiaryRepository.save(accountBeneficiary);

        log.info("Beneficiary added for account: {}", accountId);

        return mapToBeneficiaryResponse(accountBeneficiary);
    }

    public List<BeneficiaryResponse> getAccountBeneficiaries(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        List<AccountBeneficiary> beneficiaries = beneficiaryRepository.findByAccount(account);

        return beneficiaries.stream()
                .map(this::mapToBeneficiaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeBeneficiary(Long accountId, Long beneficiaryId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        AccountBeneficiary accountBeneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary", "id", beneficiaryId));

        if (!accountBeneficiary.getAccount().getId().equals(accountId)) {
            throw new ResourceNotFoundException("Beneficiary not found for account");
        }

        beneficiaryRepository.delete(accountBeneficiary);

        log.info("Beneficiary removed: {} from account: {}", beneficiaryId, accountId);
    }

    @Transactional
    public void verifyBeneficiary(Long beneficiaryId) {
        AccountBeneficiary accountBeneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary", "id", beneficiaryId));

        accountBeneficiary.setVerified(true);
        beneficiaryRepository.save(accountBeneficiary);

        log.info("Beneficiary verified: {}", beneficiaryId);
    }

    private BeneficiaryResponse mapToBeneficiaryResponse(AccountBeneficiary accountBeneficiary) {
        return BeneficiaryResponse.builder()
                .beneficiaryId(accountBeneficiary.getId())
                .beneficiaryName(accountBeneficiary.getBeneficiaryName())
                .beneficiaryAccountNumber(accountBeneficiary.getBeneficiaryAccountNumber())
                .ifscCode(accountBeneficiary.getIfscCode())
                .bankName(accountBeneficiary.getBankName())
                .verified(accountBeneficiary.isVerified())
                .remarks(accountBeneficiary.getRemarks())
                .createdAt(accountBeneficiary.getCreatedAt())
                .build();
    }
}