package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.account.repository.CustomerRepository;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.transaction.dto.request.CreateUpiIdRequest;
import com.dvein.banking_backend.transaction.dto.request.UpdateUpiIdRequest;
import com.dvein.banking_backend.transaction.dto.response.UpiIdResponse;
import com.dvein.banking_backend.transaction.dto.response.UpiProfileResponse;
import com.dvein.banking_backend.transaction.exception.InvalidUpiIdException;
import com.dvein.banking_backend.transaction.model.UpiId;
import com.dvein.banking_backend.transaction.model.UpiProfile;
import com.dvein.banking_backend.transaction.repository.UpiIdRepository;
import com.dvein.banking_backend.transaction.repository.UpiProfileRepository;
import com.dvein.banking_backend.transaction.validation.UpiValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpiService {

    private final UpiProfileRepository upiProfileRepository;
    private final UpiIdRepository upiIdRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final UpiValidator upiValidator;
    private final UpiPinService upiPinService;

    @Transactional
    public UpiProfileResponse createUpiProfile(String email) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        if (upiProfileRepository.existsByCustomer(customer)) {
            throw new InvalidRequestException("UPI profile already exists");
        }

        UpiProfile profile = UpiProfile.builder()
                .customer(customer)
                .active(true)
                .build();

        profile = upiProfileRepository.save(profile);
        log.info("UPI profile created for customer: {}", customer.getId());

        return mapToProfileResponse(profile);
    }

    public UpiProfileResponse getUpiProfile(String email) {
        UpiProfile profile = upiProfileRepository.findByCustomerUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("UPI profile not found"));

        return mapToProfileResponse(profile);
    }

    @Transactional
    public UpiIdResponse createUpiId(CreateUpiIdRequest request, String email) {
        upiValidator.validateUpiHandle(request.getHandle());

        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        UpiProfile profile = upiProfileRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("UPI profile not found. Please create profile first."));

        String upiId = request.getHandle() + "@dveinbank";
        upiValidator.validateUpiIdFormat(upiId);

        if (upiIdRepository.existsByUpiId(upiId)) {
            throw new InvalidUpiIdException("UPI ID already exists: " + upiId);
        }

        Account linkedAccount = accountRepository.findByIdAndCustomerUserEmail(request.getLinkedAccountId(), email)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", request.getLinkedAccountId()));

        // If this is the first UPI ID, make it primary
        boolean isPrimary = request.getSetPrimary() != null ? request.getSetPrimary() :
                !upiIdRepository.findByUpiProfile(profile).isEmpty() ? false : true;

        if (isPrimary) {
            // Remove primary from existing IDs
            upiIdRepository.findByUpiProfileAndPrimaryTrue(profile)
                    .ifPresent(existingPrimary -> {
                        existingPrimary.setPrimary(false);
                        upiIdRepository.save(existingPrimary);
                    });
        }

        UpiId newUpiId = UpiId.builder()
                .upiProfile(profile)
                .upiId(upiId)
                .linkedAccount(linkedAccount)
                .primary(isPrimary)
                .active(true)
                .verified(true)
                .build();

        newUpiId = upiIdRepository.save(newUpiId);

        if (isPrimary) {
            profile.setPrimaryUpiId(newUpiId);
            upiProfileRepository.save(profile);
        }

        log.info("UPI ID created: {} for customer: {}", upiId, customer.getId());

        return mapToUpiIdResponse(newUpiId);
    }

    @Transactional
    public UpiIdResponse updateUpiId(Long upiIdId, UpdateUpiIdRequest request, String email) {
        UpiId upiId = upiIdRepository.findByIdAndUserEmail(upiIdId, email)
                .orElseThrow(() -> new ResourceNotFoundException("UPI ID", "id", upiIdId));

        Account newLinkedAccount = accountRepository.findByIdAndCustomerUserEmail(request.getLinkedAccountId(), email)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", request.getLinkedAccountId()));

        upiId.setLinkedAccount(newLinkedAccount);
        upiId = upiIdRepository.save(upiId);

        log.info("UPI ID updated: {}", upiId.getUpiId());

        return mapToUpiIdResponse(upiId);
    }

    @Transactional
    public void deleteUpiId(Long upiIdId, String email) {
        UpiId upiId = upiIdRepository.findByIdAndUserEmail(upiIdId, email)
                .orElseThrow(() -> new ResourceNotFoundException("UPI ID", "id", upiIdId));

        if (upiId.isPrimary()) {
            throw new InvalidRequestException("Cannot delete primary UPI ID. Set another UPI ID as primary first.");
        }

        upiIdRepository.delete(upiId);
        log.info("UPI ID deleted: {}", upiId.getUpiId());
    }

    @Transactional
    public void setPrimaryUpiId(Long upiIdId, String email) {
        UpiId upiId = upiIdRepository.findByIdAndUserEmail(upiIdId, email)
                .orElseThrow(() -> new ResourceNotFoundException("UPI ID", "id", upiIdId));

        UpiProfile profile = upiId.getUpiProfile();

        // Remove primary from existing
        upiIdRepository.findByUpiProfileAndPrimaryTrue(profile)
                .ifPresent(existingPrimary -> {
                    existingPrimary.setPrimary(false);
                    upiIdRepository.save(existingPrimary);
                });

        // Set new primary
        upiId.setPrimary(true);
        upiIdRepository.save(upiId);

        profile.setPrimaryUpiId(upiId);
        upiProfileRepository.save(profile);

        log.info("Primary UPI ID set: {}", upiId.getUpiId());
    }

    public boolean verifyUpiId(String upiId) {
        upiValidator.validateUpiIdFormat(upiId);
        return upiIdRepository.existsByUpiId(upiId);
    }

    public UpiId getUpiIdByUpiIdString(String upiId) {
        return upiIdRepository.findByUpiId(upiId)
                .orElseThrow(() -> new InvalidUpiIdException("UPI ID not found: " + upiId));
    }

    private UpiProfileResponse mapToProfileResponse(UpiProfile profile) {
        List<UpiIdResponse> upiIds = upiIdRepository.findByUpiProfile(profile)
                .stream()
                .map(this::mapToUpiIdResponse)
                .collect(Collectors.toList());

        return UpiProfileResponse.builder()
                .id(profile.getId())
                .customerId(profile.getCustomer().getId())
                .primaryUpiId(profile.getPrimaryUpiId() != null ? profile.getPrimaryUpiId().getUpiId() : null)
                .upiIds(upiIds)
                .pinSet(upiPinService.isPinSet(profile))
                .active(profile.isActive())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    private UpiIdResponse mapToUpiIdResponse(UpiId upiId) {
        return UpiIdResponse.builder()
                .id(upiId.getId())
                .upiId(upiId.getUpiId())
                .linkedAccountNumber(upiId.getLinkedAccount() != null ? upiId.getLinkedAccount().getAccountNumber() : null)
                .primary(upiId.isPrimary())
                .active(upiId.isActive())
                .verified(upiId.isVerified())
                .createdAt(upiId.getCreatedAt())
                .build();
    }
}