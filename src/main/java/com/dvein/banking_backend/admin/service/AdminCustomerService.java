package com.dvein.banking_backend.admin.service;

import com.dvein.banking_backend.account.dto.request.KycSubmissionRequest;
import com.dvein.banking_backend.account.dto.response.CustomerProfileResponse;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.repository.CustomerRepository;
import com.dvein.banking_backend.account.service.CustomerService;
import com.dvein.banking_backend.account.service.KycService;
import com.dvein.banking_backend.admin.dto.request.CustomerSearchRequest;
import com.dvein.banking_backend.admin.dto.response.CustomerListResponse;
import com.dvein.banking_backend.common.dto.PageResponse;
import com.dvein.banking_backend.common.enums.CustomerStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final KycService kycService;

    public CustomerListResponse searchCustomers(CustomerSearchRequest request) {
        Sort sort = Sort.by(
                request.getSortDirection().equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
                request.getSortBy()
        );

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<CustomerProfileResponse> customersPage;

        if (request.getSearchTerm() != null && !request.getSearchTerm().isEmpty()) {
            customersPage = customerService.searchCustomers(request.getSearchTerm(), pageable);
        } else if (request.getStatus() != null) {
            customersPage = customerRepository.findByStatus(request.getStatus(), pageable)
                    .map(this::mapToProfileResponse);
        } else {
            customersPage = customerRepository.findAll(pageable)
                    .map(this::mapToProfileResponse);
        }

        PageResponse<CustomerProfileResponse> pageResponse = PageResponse.<CustomerProfileResponse>builder()
                .content(customersPage.getContent())
                .pageNumber(customersPage.getNumber())
                .pageSize(customersPage.getSize())
                .totalElements(customersPage.getTotalElements())
                .totalPages(customersPage.getTotalPages())
                .last(customersPage.isLast())
                .first(customersPage.isFirst())
                .build();

        return CustomerListResponse.builder()
                .customers(pageResponse)
                .totalCount(customerRepository.count())
                .activeCount(customerRepository.countByStatus(CustomerStatus.ACTIVE))
                .blockedCount(customerRepository.countByStatus(CustomerStatus.BLOCKED))
                .build();
    }

    @Transactional
    public void updateCustomerStatus(Long customerId, CustomerStatus status, String reason) {
        customerService.updateCustomerStatus(customerId, status, reason);
        log.info("Admin updated customer status: {} to {}", customerId, status);
    }

    @Transactional
    public void approveKyc(Long customerId, String approvedBy) {
        // FIX: Use approveKycByCustomerId — the path variable is customerId (Customer.id),
        // NOT userId (User.id). The old approveKyc(userId) caused "Customer not found" errors.
        kycService.approveKycByCustomerId(customerId, approvedBy);
        log.info("Admin approved KYC for customerId: {} by {}", customerId, approvedBy);
    }

    @Transactional
    public void rejectKyc(Long customerId, String reason) {
        // FIX: Use rejectKycByCustomerId — same reason as approveKyc above
        kycService.rejectKycByCustomerId(customerId, reason);
        log.info("Admin rejected KYC for customerId: {} - Reason: {}", customerId, reason);
    }

    public CustomerProfileResponse getCustomerDetails(Long customerId) {
        return customerService.getCustomerProfileByCustomerId(customerId);
    }

    private CustomerProfileResponse mapToProfileResponse(Customer customer) {
        return CustomerProfileResponse.builder()
                .customerId(customer.getId())
                .email(customer.getUser().getEmail())
                .phone(customer.getUser().getPhone())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .fullName(customer.getFullName())
                .dateOfBirth(customer.getDateOfBirth() != null ? customer.getDateOfBirth().toString() : null)
                .address(customer.getAddress())
                .city(customer.getCity())
                .state(customer.getState())
                .postalCode(customer.getPostalCode())
                .country(customer.getCountry())
                .pan(customer.getPan())
                .aadhaar(customer.getAadhaar())
                .status(customer.getStatus())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}