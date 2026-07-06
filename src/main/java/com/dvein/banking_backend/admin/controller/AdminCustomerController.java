package com.dvein.banking_backend.admin.controller;

import com.dvein.banking_backend.account.dto.response.CustomerProfileResponse;
import com.dvein.banking_backend.account.service.CustomerService;
import com.dvein.banking_backend.account.service.KycService;
import com.dvein.banking_backend.admin.dto.request.ApproveKycRequest;
import com.dvein.banking_backend.admin.dto.request.CustomerSearchRequest;
import com.dvein.banking_backend.admin.dto.request.RejectKycRequest;
import com.dvein.banking_backend.admin.dto.request.UpdateCustomerStatusRequest;
import com.dvein.banking_backend.admin.dto.response.CustomerListResponse;
import com.dvein.banking_backend.admin.service.AdminCustomerService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
@RequireRole(value = {UserRole.ADMIN, UserRole.SUPER_ADMIN})
@Tag(name = "Admin Customer Management", description = "Admin customer management endpoints")
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    @PostMapping("/search")
    @Operation(summary = "Search customers", description = "Search and filter customers")
    @RateLimited(limit = 60, duration = 60, keyType = RateLimited.KeyType.USER)
    public ResponseEntity<ApiResponse<CustomerListResponse>> searchCustomers(
            @Valid @RequestBody CustomerSearchRequest request) {
        CustomerListResponse response = adminCustomerService.searchCustomers(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer details", description = "Get detailed customer information")
    @RateLimited(limit = 60, duration = 60, keyType = RateLimited.KeyType.USER)
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getCustomerDetails(
            @PathVariable Long customerId) {
        CustomerProfileResponse response = adminCustomerService.getCustomerDetails(customerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{customerId}/status")
    @Operation(summary = "Update customer status", description = "Update customer status (Active/Blocked/Suspended)")
    @RateLimited(limit = 30, duration = 60, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.UPDATE, entityType = "Customer", description = "Customer status updated")
    public ResponseEntity<ApiResponse<Void>> updateCustomerStatus(
            @PathVariable Long customerId,
            @Valid @RequestBody UpdateCustomerStatusRequest request) {
        adminCustomerService.updateCustomerStatus(customerId, request.getStatus(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.CUSTOMER_STATUS_UPDATED, null));
    }

    @PostMapping("/{customerId}/kyc/approve")
    @Operation(summary = "Approve KYC", description = "Approve customer KYC documents")
    @RateLimited(limit = 30, duration = 60, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.KYC_APPROVE, entityType = "KYC", description = "Customer KYC approved")
    public ResponseEntity<ApiResponse<Void>> approveKyc(
            @PathVariable Long customerId,
            @Valid @RequestBody ApproveKycRequest request) {
        adminCustomerService.approveKyc(customerId, request.getApprovedBy());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.KYC_APPROVED, null));
    }

    @PostMapping("/{customerId}/kyc/reject")
    @Operation(summary = "Reject KYC", description = "Reject customer KYC documents")
    @RateLimited(limit = 30, duration = 60, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.KYC_REJECT, entityType = "KYC", description = "Customer KYC rejected")
    public ResponseEntity<ApiResponse<Void>> rejectKyc(
            @PathVariable Long customerId,
            @Valid @RequestBody RejectKycRequest request) {
        adminCustomerService.rejectKyc(customerId, request.getReason());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.KYC_REJECTED, null));
    }
}