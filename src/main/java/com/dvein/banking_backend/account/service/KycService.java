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

    // =========================================================================
    // Customer self-service (userId based — user looks up their own KYC)
    // =========================================================================

    @Transactional
    public KycStatusResponse submitKyc(Long userId, KycSubmissionRequest request) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "userId", userId));

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

        kyc.setStatus(KycStatus.SUBMITTED);
        kyc.setSubmittedAt(LocalDateTime.now());
        kyc = kycRepository.save(kyc);

        log.info("KYC submitted for userId: {}", userId);

        return mapToKycStatusResponse(kyc);
    }

    public KycStatusResponse getKycStatus(Long userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "userId", userId));

        Kyc kyc = kycRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("KYC", "customer", userId));

        return mapToKycStatusResponse(kyc);
    }

    // =========================================================================
    // Admin flows (customerId based — admin operates on Customer.id, NOT User.id)
    // =========================================================================

    /**
     * Admin: Approve KYC by Customer ID (Customer.id, NOT User.id).
     * <p>
     * This fixes the bug where the admin endpoint path variable {customerId}
     * was incorrectly passed to findByUserId(), causing "Customer not found"
     * errors when customerId != userId (e.g., admin has userId=1, first
     * customer has customerId=1 but userId=2).
     */
    @Transactional
    @Audited(action = AuditAction.KYC_APPROVE, entityType = "KYC", description = "KYC approved by admin")
    public void approveKycByCustomerId(Long customerId, String approvedBy) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Kyc kyc = kycRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("KYC", "customerId", customerId));

        if (kyc.getStatus() == KycStatus.VERIFIED) {
            throw new InvalidRequestException("KYC is already approved");
        }

        kyc.setStatus(KycStatus.VERIFIED);
        kyc.setApprovedAt(LocalDateTime.now());
        kyc.setApprovedBy(approvedBy);
        kyc.setExpiryDate(LocalDate.now().plusYears(1));
        kycRepository.save(kyc);

        log.info("KYC approved for customerId: {} by admin: {}", customerId, approvedBy);
    }

    /**
     * Admin: Reject KYC by Customer ID (Customer.id, NOT User.id).
     */
    @Transactional
    @Audited(action = AuditAction.KYC_REJECT, entityType = "KYC", description = "KYC rejected by admin")
    public void rejectKycByCustomerId(Long customerId, String reason) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Kyc kyc = kycRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("KYC", "customerId", customerId));

        if (kyc.getStatus() == KycStatus.VERIFIED) {
            throw new InvalidRequestException("Cannot reject an already approved KYC");
        }

        kyc.setStatus(KycStatus.REJECTED);
        kyc.setRejectionReason(reason);
        kyc.setRejectedAt(LocalDateTime.now());
        kycRepository.save(kyc);

        log.info("KYC rejected for customerId: {} - Reason: {}", customerId, reason);
    }

    // =========================================================================
    // Private mapping
    // =========================================================================

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