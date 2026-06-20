package com.dvein.banking_backend.account.controller;

import com.dvein.banking_backend.account.dto.response.AccountVerificationResponse;
import com.dvein.banking_backend.account.service.VerificationService;
import com.dvein.banking_backend.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/verification")
@RequiredArgsConstructor
@Tag(name = "Verification", description = "Verification endpoints for other modules")
public class VerificationController {

    private final VerificationService verificationService;

    @GetMapping("/account/{accountNumber}")
    @Operation(summary = "Verify account", description = "Verify account existence and status")
    public ResponseEntity<ApiResponse<AccountVerificationResponse>> verifyAccount(
            @PathVariable String accountNumber) {
        AccountVerificationResponse response = verificationService.verifyAccount(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Verify customer", description = "Verify customer existence and status")
    public ResponseEntity<ApiResponse<Boolean>> verifyCustomer(@PathVariable Long customerId) {
        boolean exists = verificationService.verifyCustomerExists(customerId);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @GetMapping("/account/{accountNumber}/status")
    @Operation(summary = "Verify account status", description = "Verify if account is active")
    public ResponseEntity<ApiResponse<Boolean>> verifyAccountStatus(@PathVariable String accountNumber) {
        boolean status = verificationService.verifyAccountStatus(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @GetMapping("/kyc/{customerId}")
    @Operation(summary = "Verify KYC", description = "Verify if customer KYC is approved")
    public ResponseEntity<ApiResponse<Boolean>> verifyKyc(@PathVariable Long customerId) {
        boolean kycVerified = verificationService.verifyKycStatus(customerId);
        return ResponseEntity.ok(ApiResponse.success(kycVerified));
    }
}