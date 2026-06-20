package com.dvein.banking_backend.account.controller;

import com.dvein.banking_backend.account.dto.request.UpdateProfileRequest;
import com.dvein.banking_backend.account.dto.response.CustomerProfileResponse;
import com.dvein.banking_backend.account.service.CustomerService;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.annotation.RequireRole;
import com.dvein.banking_backend.common.constant.SuccessMessages;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.dto.PageResponse;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.common.enums.UserRole;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
@RequireRole(UserRole.CUSTOMER)
@Tag(name = "Customer Management", description = "Customer profile and management endpoints")
public class CustomerController {

    private final CustomerService customerService;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;

    @GetMapping("/profile")
    @Operation(summary = "Get customer profile", description = "Get current customer's profile")
    @RateLimited(limit = 30, duration = 60, keyType = RateLimited.KeyType.USER)
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getProfile() {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerProfileResponse profile = customerService.getCustomerProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update customer profile", description = "Update customer profile details")
    @RateLimited(limit = 10, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.UPDATE, entityType = "Customer", description = "Profile updated")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerProfileResponse updatedProfile = customerService.updateCustomerProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.PROFILE_UPDATED, updatedProfile));
    }
}