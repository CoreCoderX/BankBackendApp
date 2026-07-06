package com.dvein.banking_backend.transaction.controller;

import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.transaction.dto.response.TransactionReceiptResponse;
import com.dvein.banking_backend.transaction.service.TransactionReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Receipts", description = "Transaction receipt endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('CUSTOMER')")
public class TransactionReceiptController {

    private final TransactionReceiptService receiptService;
    private final SecurityContextHelper securityContextHelper;

    @PostMapping("/{transactionId}/receipt")
    @Operation(summary = "Generate receipt", description = "Generate transaction receipt")
    @RateLimited
    public ResponseEntity<ApiResponse<TransactionReceiptResponse>> generateReceipt(@PathVariable Long transactionId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        TransactionReceiptResponse response = receiptService.generateReceipt(transactionId, email);
        return ResponseEntity.ok(ApiResponse.success("Receipt generated successfully", response));
    }

    @GetMapping("/{transactionId}/receipt")
    @Operation(summary = "Get receipt", description = "Get transaction receipt")
    @RateLimited
    public ResponseEntity<ApiResponse<TransactionReceiptResponse>> getReceipt(@PathVariable Long transactionId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        TransactionReceiptResponse response = receiptService.getReceipt(transactionId, email);
        return ResponseEntity.ok(ApiResponse.success("Receipt retrieved successfully", response));
    }
}