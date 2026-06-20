package com.dvein.banking_backend.account.service;

import com.dvein.banking_backend.account.dto.request.KycSubmissionRequest;
import com.dvein.banking_backend.account.dto.response.KycStatusResponse;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.model.Kyc;
import com.dvein.banking_backend.account.repository.CustomerRepository;
import com.dvein.banking_backend.account.repository.KycRepository;
import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.common.enums.KycStatus;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class KycService {

    private final KycRepository kycRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public KycStatusResponse submitKyc(Long customerId, KycSubmissionRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Kyc kyc = kycRepository.findByCustomer(customer)
                .orElse(Kyc.builder()
                        .customer(customer)
                        .build());

        // Update customer PAN and Aadhaar
        customer.setPan(request.getPan());
        customer.setAadhaar(request.getAadhaar());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPostalCode(request.getPostalCode());
        customerRepository.save(customer);

        // Update KYC status
        kyc.setStatus(KycStatus.SUBMITTED);
        kyc.setSubmittedAt(LocalDateTime.now());
        kyc = kycRepository.save(kyc);

        log.info("KYC submitted for customer: {}", customerId);

        return mapToKycStatusResponse(kyc);
    }

    public KycStatusResponse getKycStatus(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Kyc kyc = kycRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("KYC", "customer", customerId));

        return mapToKycStatusResponse(kyc);
    }

    @Transactional
    @Audited(action = AuditAction.KYC_APPROVE, entityType = "KYC", description = "KYC approved")
    public void approveKyc(Long customerId, String approvedBy) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Kyc kyc = kycRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("KYC", "customer", customerId));

        kyc.setStatus(KycStatus.VERIFIED);
        kyc.setApprovedAt(LocalDateTime.now());
        kyc.setApprovedBy(approvedBy);
        kyc.setExpiryDate(LocalDate.now().plusYears(1));
        kycRepository.save(kyc);

        log.info("KYC approved for customer: {} by {}", customerId, approvedBy);
    }

    @Transactional
    @Audited(action = AuditAction.KYC_REJECT, entityType = "KYC", description = "KYC rejected")
    public void rejectKyc(Long customerId, String reason) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Kyc kyc = kycRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("KYC", "customer", customerId));

        kyc.setStatus(KycStatus.REJECTED);
        kyc.setRejectionReason(reason);
        kyc.setRejectedAt(LocalDateTime.now());
        kycRepository.save(kyc);

        log.info("KYC rejected for customer: {} - Reason: {}", customerId, reason);
    }

    private KycStatusResponse mapToKycStatusResponse(Kyc kyc) {
        return KycStatusResponse.builder()
                .kycId(kyc.getId())
                .status(kyc.getStatus())
                .rejectionReason(kyc.getRejectionReason())
                .submittedAt(kyc.getSubmittedAt())
                .approvedAt(kyc.getApprovedAt())
                .approvedBy(kyc.getApprovedBy())
                .expiryDate(kyc.getExpiryDate())
                .expired(kyc.isExpired())
                .build();
    }
}