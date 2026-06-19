package com.dvein.banking_backend.account.service;

import com.dvein.banking_backend.account.dto.request.UpdateProfileRequest;
import com.dvein.banking_backend.account.dto.response.CustomerProfileResponse;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.repository.CustomerRepository;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.enums.CustomerStatus;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public CustomerProfileResponse getCustomerProfile(Long userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer", "userId", userId));

        return mapToProfileResponse(customer);
    }

    @Transactional
    public CustomerProfileResponse updateCustomerProfile(Long userId,
                                                         UpdateProfileRequest request) {

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer", "userId", userId));

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setMiddleName(request.getMiddleName());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPostalCode(request.getPostalCode());
        customer.setCountry(request.getCountry());

        // Update user phone
        User user = customer.getUser();
        user.setPhone(request.getPhone());
        userRepository.save(user);

        customer = customerRepository.save(customer);

        log.info("Customer profile updated: {}", userId);

        return mapToProfileResponse(customer);
    }

    public Page<CustomerProfileResponse> searchCustomers(String searchTerm, Pageable pageable) {
        return customerRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(searchTerm, searchTerm, pageable)
                .map(this::mapToProfileResponse);
    }

    @Transactional
    public void updateCustomerStatus(Long customerId, CustomerStatus status, String reason) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        customer.setStatus(status);
        if (reason != null) {
            customer.setSuspensionReason(reason);
        }

        customerRepository.save(customer);

        log.info("Customer status updated: {} - Status: {}", customerId, status);
    }

    public long getActiveCutomersCount() {
        return customerRepository.countByStatus(CustomerStatus.ACTIVE);
    }

    public long getBlockedCustomersCount() {
        return customerRepository.countByStatus(CustomerStatus.BLOCKED);
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
                .profilePhotoUrl(customer.getProfilePhotoUrl())
                .status(customer.getStatus())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}