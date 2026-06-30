package com.dvein.banking_backend.transaction.controller;

import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.dto.PageResponse;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.transaction.dto.request.RefundRequest;
import com.dvein.banking_backend.transaction.dto.request.ReversalRequest;
import com.dvein.banking_backend.transaction.dto.response.TransactionResponse;
import com.dvein.banking_backend.transaction.service.AdminTransactionService;
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
@RequestMapping("/admin/transactions")
@RequiredArgsConstructor
@Tag(name = "Admin Transaction Management", description = "Admin transaction management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminTransactionController {

    private final AdminTransactionService adminTransactionService;
    private final SecurityContextHelper securityContextHelper;

    @GetMapping
    @Operation(summary = "Get all transactions", description = "Get all transactions with pagination")
    @RateLimited
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "initiatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        PageResponse<TransactionResponse> response = adminTransactionService.getAllTransactions(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", response));
    }

    @GetMapping("/pending-approvals")
    @Operation(summary = "Get pending approvals", description = "Get transactions pending admin approval")
    @RateLimited
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getPendingApprovals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<TransactionResponse> response = adminTransactionService.getPendingApprovals(page, size);
        return ResponseEntity.ok(ApiResponse.success("Pending approvals retrieved successfully", response));
    }

    @GetMapping("/failed")
    @Operation(summary = "Get failed transactions", description = "Get all failed transactions")
    @RateLimited
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getFailedTransactions() {
        List<TransactionResponse> response = adminTransactionService.getFailedTransactions();
        return ResponseEntity.ok(ApiResponse.success("Failed transactions retrieved successfully", response));
    }

    @PostMapping("/{transactionId}/reverse")
    @Operation(summary = "Reverse transaction", description = "Reverse a completed transaction")
    @RateLimited
    @Audited(action = AuditAction.UPDATE, entityType = "Transaction", description = "Transaction reversed")
    public ResponseEntity<ApiResponse<Void>> reverseTransaction(
            @PathVariable Long transactionId,
            @Valid @RequestBody ReversalRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        adminTransactionService.reverseTransaction(transactionId, request.getReason(), email);
        return ResponseEntity.ok(ApiResponse.success("Transaction reversed successfully", null));
    }

    @PostMapping("/{transactionId}/refund")
    @Operation(summary = "Refund transaction", description = "Refund a completed transaction")
    @RateLimited
    @Audited(action = AuditAction.UPDATE, entityType = "Transaction", description = "Transaction refunded")
    public ResponseEntity<ApiResponse<Void>> refundTransaction(
            @PathVariable Long transactionId,
            @Valid @RequestBody RefundRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        adminTransactionService.refundTransaction(transactionId, request.getReason(), email);
        return ResponseEntity.ok(ApiResponse.success("Transaction refunded successfully", null));
    }
}