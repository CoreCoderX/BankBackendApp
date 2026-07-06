package com.dvein.banking_backend.transaction.controller;

import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.transaction.dto.request.CreateMerchantRequest;
import com.dvein.banking_backend.transaction.dto.request.UpdateMerchantRequest;
import com.dvein.banking_backend.transaction.dto.response.MerchantResponse;
import com.dvein.banking_backend.transaction.service.AdminMerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/merchants")
@RequiredArgsConstructor
@Tag(name = "Admin Merchant Management", description = "Admin merchant management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminMerchantController {

    private final AdminMerchantService adminMerchantService;

    @PostMapping
    @Operation(summary = "Create merchant", description = "Create new merchant")
    @RateLimited
    @Audited(action = AuditAction.CREATE, entityType = "Merchant", description = "Merchant created")
    public ResponseEntity<ApiResponse<MerchantResponse>> createMerchant(@Valid @RequestBody CreateMerchantRequest request) {
        MerchantResponse response = adminMerchantService.createMerchant(request);
        return ResponseEntity.ok(ApiResponse.success("Merchant created successfully", response));
    }

    @PutMapping("/{merchantId}")
    @Operation(summary = "Update merchant", description = "Update merchant details")
    @RateLimited
    @Audited(action = AuditAction.UPDATE, entityType = "Merchant", description = "Merchant updated")
    public ResponseEntity<ApiResponse<MerchantResponse>> updateMerchant(
            @PathVariable Long merchantId,
            @Valid @RequestBody UpdateMerchantRequest request) {
        MerchantResponse response = adminMerchantService.updateMerchant(merchantId, request);
        return ResponseEntity.ok(ApiResponse.success("Merchant updated successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all merchants", description = "Get all merchants")
    @RateLimited
    public ResponseEntity<ApiResponse<List<MerchantResponse>>> getAllMerchants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<MerchantResponse> response = adminMerchantService.getAllMerchants(page, size);
        return ResponseEntity.ok(ApiResponse.success("Merchants retrieved successfully", response));
    }

    @DeleteMapping("/{merchantId}")
    @Operation(summary = "Delete merchant", description = "Deactivate merchant")
    @RateLimited
    @Audited(action = AuditAction.DELETE, entityType = "Merchant", description = "Merchant deleted")
    public ResponseEntity<ApiResponse<Void>> deleteMerchant(@PathVariable Long merchantId) {
        adminMerchantService.deleteMerchant(merchantId);
        return ResponseEntity.ok(ApiResponse.success("Merchant deleted successfully", null));
    }
}