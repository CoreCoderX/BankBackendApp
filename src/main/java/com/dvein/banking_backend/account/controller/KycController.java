package com.dvein.banking_backend.account.controller;

import com.dvein.banking_backend.account.dto.request.KycSubmissionRequest;
import com.dvein.banking_backend.account.dto.response.KycStatusResponse;
import com.dvein.banking_backend.account.service.KycService;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.annotation.RequireRole;
import com.dvein.banking_backend.common.constant.SuccessMessages;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.common.enums.UserRole;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kyc")
@RequiredArgsConstructor
@RequireRole(UserRole.CUSTOMER)
@Tag(name = "KYC Management", description = "Know Your Customer (KYC) endpoints")
public class KycController {

    private final KycService kycService;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;

    @PostMapping("/submit")
    @Operation(summary = "Submit KYC", description = "Submit KYC documents and information")
    @RateLimited(limit = 5, duration = 86400, keyType = RateLimited.KeyType.USER,
            message = "KYC submission limit reached. Please contact support.")
    @Audited(action = AuditAction.CREATE, entityType = "KYC", description = "KYC submitted")
    public ResponseEntity<ApiResponse<KycStatusResponse>> submitKyc(
            @Valid @RequestBody KycSubmissionRequest request) {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        KycStatusResponse kycStatus = kycService.submitKyc(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.KYC_SUBMITTED, kycStatus));
    }

    @GetMapping("/status")
    @Operation(summary = "Get KYC status", description = "Get current KYC status")
    @RateLimited(limit = 30, duration = 60, keyType = RateLimited.KeyType.USER)
    public ResponseEntity<ApiResponse<KycStatusResponse>> getKycStatus() {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        KycStatusResponse kycStatus = kycService.getKycStatus(user.getId());
        return ResponseEntity.ok(ApiResponse.success(kycStatus));
    }
}