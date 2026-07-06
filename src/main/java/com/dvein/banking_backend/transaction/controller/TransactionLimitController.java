package com.dvein.banking_backend.transaction.controller;

import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.transaction.dto.request.UpdateTransactionLimitRequest;
import com.dvein.banking_backend.transaction.dto.response.TransactionLimitResponse;
import com.dvein.banking_backend.transaction.service.TransactionLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction-limits")
@RequiredArgsConstructor
@Tag(name = "Transaction Limits", description = "Transaction limit management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('CUSTOMER')")
public class TransactionLimitController {

    private final TransactionLimitService limitService;
    private final SecurityContextHelper securityContextHelper;

    @GetMapping
    @Operation(summary = "Get limits", description = "Get transaction limits")
    @RateLimited
    public ResponseEntity<ApiResponse<TransactionLimitResponse>> getLimits() {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        TransactionLimitResponse response = limitService.getLimits(email);
        return ResponseEntity.ok(ApiResponse.success("Transaction limits retrieved successfully", response));
    }

    @PutMapping
    @Operation(summary = "Update limits", description = "Update transaction limits")
    @RateLimited
    public ResponseEntity<ApiResponse<TransactionLimitResponse>> updateLimits(
            @Valid @RequestBody UpdateTransactionLimitRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        TransactionLimitResponse response = limitService.updateLimits(request, email);
        return ResponseEntity.ok(ApiResponse.success("Transaction limits updated successfully", response));
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset limits", description = "Reset transaction limits to default")
    @RateLimited
    public ResponseEntity<ApiResponse<Void>> resetLimits() {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        limitService.resetLimits(email);
        return ResponseEntity.ok(ApiResponse.success("Transaction limits reset successfully", null));
    }
}