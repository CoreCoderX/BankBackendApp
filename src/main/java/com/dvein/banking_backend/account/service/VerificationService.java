package com.dvein.banking_backend.account.service;

import com.dvein.banking_backend.account.dto.response.AccountVerificationResponse;
import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.model.Kyc;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.account.repository.CustomerRepository;
import com.dvein.banking_backend.account.repository.KycRepository;
import com.dvein.banking_backend.common.enums.KycStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final KycRepository kycRepository;

    public boolean verifyCustomerExists(Long customerId) {
        return customerRepository.existsById(customerId);
    }

    public boolean verifyAccountExists(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }

    public AccountVerificationResponse verifyAccount(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);

        if (accountOpt.isEmpty()) {
            return AccountVerificationResponse.builder()
                    .exists(false)
                    .build();
        }

        Account account = accountOpt.get();
        Customer customer = account.getCustomer();

        var kyc = kycRepository.findByCustomer(customer);
        String kycStatus = kyc.isPresent() ? kyc.get().getStatus().name() : "NOT_SUBMITTED";

        return AccountVerificationResponse.builder()
                .exists(true)
                .accountNumber(account.getAccountNumber())
                .accountHolderName(customer.getFullName())
                .accountStatus(account.getStatus())
                .customerStatus(customer.getStatus())
                .kycStatus(kycStatus)
                .availableBalance(account.getBalance())
                .customerId(customer.getId())
                .build();
    }

    public boolean verifyAccountStatus(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> account.getStatus().name().equals("ACTIVE"))
                .orElse(false);
    }

    public boolean verifyCustomerStatus(Long customerId) {
        return customerRepository.findById(customerId)
                .map(customer -> customer.getStatus().name().equals("ACTIVE"))
                .orElse(false);
    }

    public boolean verifyKycStatus(Long customerId) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) return false;

        Optional<Kyc> kycOpt = kycRepository.findByCustomer(customerOpt.get());
        return kycOpt.isPresent() && kycOpt.get().getStatus().equals(KycStatus.VERIFIED);
    }
}