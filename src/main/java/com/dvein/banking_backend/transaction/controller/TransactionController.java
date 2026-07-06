package com.dvein.banking_backend.transaction.controller;

import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.transaction.dto.request.*;
import com.dvein.banking_backend.transaction.dto.response.*;
import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.model.TransactionDispute;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import com.dvein.banking_backend.transaction.service.*;
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
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "Transaction management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('CUSTOMER')")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionFeeService feeService;
    private final TransactionReceiptService receiptService;
    private final SecurityContextHelper securityContextHelper;
    private final TransactionRepository transactionRepository;

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get transaction by ID", description = "Get transaction details by ID")
    @RateLimited
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(@PathVariable Long transactionId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        TransactionResponse response = transactionService.getTransactionById(transactionId, email);
        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved successfully", response));
    }

    @GetMapping("/reference/{referenceNumber}")
    @Operation(summary = "Get transaction by reference number", description = "Get transaction details by reference number or UTR")
    @RateLimited
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByReference(@PathVariable String referenceNumber) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new com.dvein.banking_backend.common.exception.ResourceNotFoundException(
                        "Transaction", "referenceNumber", referenceNumber));

        // Verify ownership
        if (transaction.getSenderAccount() != null &&
                !transaction.getSenderAccount().getCustomer().getUser().getEmail().equals(email) &&
                transaction.getReceiverAccount() != null &&
                !transaction.getReceiverAccount().getCustomer().getUser().getEmail().equals(email)) {
            throw new com.dvein.banking_backend.common.exception.UnauthorizedException("You do not have access to this transaction");
        }

        TransactionResponse response = transactionService.getTransactionById(transaction.getId(), email);
        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved successfully", response));
    }

    @GetMapping("/status/{transactionId}")
    @Operation(summary = "Verify transaction status", description = "Check current status of a transaction")
    @RateLimited
    public ResponseEntity<ApiResponse<TransactionStatus>> verifyTransactionStatus(@PathVariable Long transactionId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        TransactionResponse txn = transactionService.getTransactionById(transactionId, email);
        return ResponseEntity.ok(ApiResponse.success("Transaction status retrieved", txn.getStatus()));
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get account transactions", description = "Get all transactions for an account")
    @RateLimited
    public ResponseEntity<ApiResponse<TransactionListResponse>> getAccountTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        TransactionListResponse response = transactionService.getMyTransactions(accountId, email, page, size);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", response));
    }

    @GetMapping("/account/{accountId}/mini-statement")
    @Operation(summary = "Get mini statement", description = "Get last 10 transactions")
    @RateLimited
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getMiniStatement(@PathVariable Long accountId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        List<TransactionResponse> response = transactionService.getMiniStatement(accountId, email);
        return ResponseEntity.ok(ApiResponse.success("Mini statement retrieved successfully", response));
    }

    @GetMapping("/account/{accountId}/summary")
    @Operation(summary = "Get transaction summary", description = "Get transaction summary for account")
    @RateLimited
    public ResponseEntity<ApiResponse<TransactionSummaryResponse>> getTransactionSummary(@PathVariable Long accountId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        TransactionSummaryResponse response = transactionService.getTransactionSummary(accountId, email);
        return ResponseEntity.ok(ApiResponse.success("Transaction summary retrieved successfully", response));
    }

    @PostMapping("/search")
    @Operation(summary = "Search transactions", description = "Advanced search and filter transactions")
    @RateLimited
    public ResponseEntity<ApiResponse<TransactionListResponse>> searchTransactions(
            @Valid @RequestBody TransactionSearchRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        // Search implementation would go here
        return ResponseEntity.ok(ApiResponse.success("Search completed", null));
    }

    @PostMapping("/charges/estimate")
    @Operation(summary = "Estimate charges", description = "Estimate transaction charges before initiating transfer")
    @RateLimited
    public ResponseEntity<ApiResponse<EstimatedChargeResponse>> estimateCharges(
            @Valid @RequestBody EstimateChargeRequest request) {
        TransactionFeeService.FeeCalculation feeCalc = feeService.calculateFee(
                com.dvein.banking_backend.transaction.enums.TransactionType.valueOf(request.getTransactionType()),
                request.getAmount()
        );

        EstimatedChargeResponse response = EstimatedChargeResponse.builder()
                .transactionType(request.getTransactionType())
                .amount(request.getAmount())
                .baseFee(feeCalc.getBaseFee())
                .gstPercentage(feeCalc.getBaseFee().compareTo(java.math.BigDecimal.ZERO) > 0 ?
                        java.math.BigDecimal.valueOf(18) : java.math.BigDecimal.ZERO)
                .gstAmount(feeCalc.getGst())
                .totalFee(feeCalc.getTotalFee())
                .totalDebitAmount(request.getAmount().add(feeCalc.getTotalFee()))
                .build();

        return ResponseEntity.ok(ApiResponse.success("Charges estimated successfully", response));
    }

    @GetMapping("/charges/{transactionType}")
    @Operation(summary = "Get transaction charges", description = "Get current charges for a transaction type")
    @RateLimited
    public ResponseEntity<ApiResponse<EstimatedChargeResponse>> getTransactionCharges(
            @PathVariable String transactionType) {
        // Return default fee config for the transaction type
        TransactionFeeService.FeeCalculation feeCalc = feeService.calculateFee(
                com.dvein.banking_backend.transaction.enums.TransactionType.valueOf(transactionType),
                java.math.BigDecimal.valueOf(1000) // Sample amount for display
        );

        EstimatedChargeResponse response = EstimatedChargeResponse.builder()
                .transactionType(transactionType)
                .amount(java.math.BigDecimal.valueOf(1000))
                .baseFee(feeCalc.getBaseFee())
                .gstPercentage(feeCalc.getBaseFee().compareTo(java.math.BigDecimal.ZERO) > 0 ?
                        java.math.BigDecimal.valueOf(18) : java.math.BigDecimal.ZERO)
                .gstAmount(feeCalc.getGst())
                .totalFee(feeCalc.getTotalFee())
                .totalDebitAmount(java.math.BigDecimal.valueOf(1000).add(feeCalc.getTotalFee()))
                .build();

        return ResponseEntity.ok(ApiResponse.success("Charges retrieved successfully", response));
    }

    @PostMapping("/{transactionId}/receipt/resend")
    @Operation(summary = "Resend receipt", description = "Resend transaction receipt to email")
    @RateLimited
    public ResponseEntity<ApiResponse<Void>> resendReceipt(@PathVariable Long transactionId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        // Generate and send receipt via email
        receiptService.generateReceipt(transactionId, email);
        return ResponseEntity.ok(ApiResponse.success("Receipt resent successfully", null));
    }

    @PostMapping("/{transactionId}/dispute")
    @Operation(summary = "Raise dispute", description = "Raise a dispute for a transaction")
    @RateLimited
    @Audited(action = AuditAction.CREATE, entityType = "TransactionDispute", description = "Dispute raised")
    public ResponseEntity<ApiResponse<TransactionDisputeDetailResponse>> raiseDispute(
            @PathVariable Long transactionId,
            @Valid @RequestBody RaiseDisputeRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        // Dispute creation logic
        return ResponseEntity.ok(ApiResponse.success("Dispute raised successfully", null));
    }

    @GetMapping("/disputes")
    @Operation(summary = "Get my disputes", description = "Get all disputes raised by customer")
    @RateLimited
    public ResponseEntity<ApiResponse<List<TransactionDisputeDetailResponse>>> getMyDisputes() {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        // Get disputes for customer
        return ResponseEntity.ok(ApiResponse.success("Disputes retrieved successfully", null));
    }
}