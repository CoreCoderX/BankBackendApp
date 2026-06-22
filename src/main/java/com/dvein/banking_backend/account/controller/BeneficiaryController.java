package com.dvein.banking_backend.account.controller;

import com.dvein.banking_backend.account.dto.request.AddBeneficiaryRequest;
import com.dvein.banking_backend.account.dto.response.BeneficiaryResponse;
import com.dvein.banking_backend.account.service.AccountBeneficiaryService;
import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.annotation.RequireRole;
import com.dvein.banking_backend.common.constant.SuccessMessages;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.common.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping("/beneficiary")
@RequiredArgsConstructor
@RequireRole(UserRole.CUSTOMER)
@Tag(name = "Beneficiary Management", description = "Beneficiary management endpoints")
@RestController
public class BeneficiaryController {

    private final AccountBeneficiaryService beneficiaryService;

    @PostMapping("/{accountId}")
    @Operation(summary = "Add beneficiary", description = "Add new beneficiary to account")
    @RateLimited(limit = 20, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.CREATE, entityType = "Beneficiary", description = "Beneficiary added")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> addBeneficiary(
            @PathVariable Long accountId,
            @Valid @RequestBody AddBeneficiaryRequest request) {
        BeneficiaryResponse beneficiary = beneficiaryService.addBeneficiary(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessMessages.BENEFICIARY_ADDED, beneficiary));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get beneficiaries", description = "Get all beneficiaries for account")
    @RateLimited(limit = 30, duration = 60, keyType = RateLimited.KeyType.USER)
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getBeneficiaries(
            @PathVariable Long accountId) {
        List<BeneficiaryResponse> beneficiaries = beneficiaryService.getAccountBeneficiaries(accountId);
        return ResponseEntity.ok(ApiResponse.success(beneficiaries));
    }

    @DeleteMapping("/{accountId}/{beneficiaryId}")
    @Operation(summary = "Remove beneficiary", description = "Remove beneficiary from account")
    @RateLimited(limit = 10, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.DELETE, entityType = "Beneficiary", description = "Beneficiary removed")
    public ResponseEntity<ApiResponse<Void>> removeBeneficiary(
            @PathVariable Long accountId,
            @PathVariable Long beneficiaryId) {
        beneficiaryService.removeBeneficiary(accountId, beneficiaryId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.BENEFICIARY_REMOVED, null));
    }
}