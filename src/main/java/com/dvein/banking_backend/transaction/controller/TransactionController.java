package com.dvein.banking_backend.transaction.controller;

import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.dto.PageResponse;
import com.dvein.banking_backend.transaction.dto.response.MiniStatementResponse;
import com.dvein.banking_backend.transaction.dto.response.TransactionDetailResponse;
import com.dvein.banking_backend.transaction.dto.response.TransactionHistoryResponse;
import com.dvein.banking_backend.transaction.dto.response.TransactionResponse;
import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.enums.TransactionType;
import com.dvein.banking_backend.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        return ResponseEntity.ok(transactionService.getAllTransactions(page, size, sortBy, sortDirection));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionDetailResponse>> getTransactionById(
            @PathVariable String transactionId
    ) {
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponse<TransactionHistoryResponse>>> getTransactionHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                transactionService.getTransactionHistory(startDate, endDate, status, type, page, size)
        );
    }

    @GetMapping("/statement")
    public ResponseEntity<ApiResponse<MiniStatementResponse>> getMiniStatement() {
        return ResponseEntity.ok(transactionService.getMiniStatement());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> searchTransactions(
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                transactionService.searchTransactions(
                        transactionId, accountNumber, minAmount, maxAmount, date, page, size
                )
        );
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTodayTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(transactionService.getTodayTransactions(page, size));
    }

    @GetMapping("/week")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getLastWeekTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(transactionService.getLastWeekTransactions(page, size));
    }

    @GetMapping("/month")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getLastMonthTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(transactionService.getLastMonthTransactions(page, size));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactionsByAccount(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccount(accountId, page, size));
    }
}