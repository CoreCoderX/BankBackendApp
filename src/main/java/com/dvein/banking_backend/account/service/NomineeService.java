package com.dvein.banking_backend.account.service;

import com.dvein.banking_backend.account.dto.request.AddNomineeRequest;
import com.dvein.banking_backend.account.dto.response.NomineeResponse;
import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Nominee;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.account.repository.NomineeRepository;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NomineeService {

    private final NomineeRepository nomineeRepository;
    private final AccountRepository accountRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public NomineeResponse addNominee(Long accountId, AddNomineeRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        Nominee nominee = Nominee.builder()
                .account(account)
                .nomineeName(request.getNomineeName())
                .nomineeDateOfBirth(request.getNomineeDateOfBirth() != null ?
                        LocalDate.parse(request.getNomineeDateOfBirth(), DATE_FORMATTER) : null)
                .nomineeRelationship(request.getNomineeRelationship())
                .nomineePhone(request.getNomineePhone())
                .nomineeEmail(request.getNomineeEmail())
                .nomineeAddress(request.getNomineeAddress())
                .percentage(request.getPercentage())
                .build();

        nominee = nomineeRepository.save(nominee);

        log.info("Nominee added for account: {}", accountId);

        return mapToNomineeResponse(nominee);
    }

    public List<NomineeResponse> getAccountNominees(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        List<Nominee> nominees = nomineeRepository.findByAccountAndActiveTrue(account);

        return nominees.stream()
                .map(this::mapToNomineeResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeNominee(Long accountId, Long nomineeId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        Nominee nominee = nomineeRepository.findById(nomineeId)
                .orElseThrow(() -> new ResourceNotFoundException("Nominee", "id", nomineeId));

        if (!nominee.getAccount().getId().equals(accountId)) {
            throw new ResourceNotFoundException("Nominee not found for account");
        }

        nominee.setActive(false);
        nomineeRepository.save(nominee);

        log.info("Nominee removed: {} from account: {}", nomineeId, accountId);
    }

    private NomineeResponse mapToNomineeResponse(Nominee nominee) {
        return NomineeResponse.builder()
                .nomineeId(nominee.getId())
                .nomineeName(nominee.getNomineeName())
                .nomineeDateOfBirth(nominee.getNomineeDateOfBirth() != null ? nominee.getNomineeDateOfBirth().toString() : null)
                .nomineeRelationship(nominee.getNomineeRelationship())
                .nomineePhone(nominee.getNomineePhone())
                .nomineeEmail(nominee.getNomineeEmail())
                .nomineeAddress(nominee.getNomineeAddress())
                .percentage(nominee.getPercentage())
                .active(nominee.isActive())
                .createdAt(nominee.getCreatedAt())
                .build();
    }
}