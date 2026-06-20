package com.dvein.banking_backend.loan.controller;

import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.dto.PageResponse;
import com.dvein.banking_backend.loan.dto.request.ApplyLoanRequest;
import com.dvein.banking_backend.loan.dto.request.LoanClosureRequest;
import com.dvein.banking_backend.loan.dto.response.*;
import com.dvein.banking_backend.loan.service.LoanClosureService;
import com.dvein.banking_backend.loan.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class LoanController {

    private final LoanService loanService;
    private final LoanClosureService loanClosureService;

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<LoanResponse>> applyLoan(
            @Valid @RequestBody ApplyLoanRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(loanService.applyLoan(request));
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<ApiResponse<LoanDetailResponse>> getLoanById(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getLoanById(loanId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LoanResponse>>> getAllLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(loanService.getAllLoans(page, size));
    }

    @GetMapping("/{loanId}/schedule")
    public ResponseEntity<ApiResponse<List<LoanScheduleResponse>>> getLoanSchedule(
            @PathVariable Long loanId
    ) {
        return ResponseEntity.ok(loanService.getLoanSchedule(loanId));
    }

    @GetMapping("/{loanId}/outstanding")
    public ResponseEntity<ApiResponse<OutstandingBalanceResponse>> getOutstandingBalance(
            @PathVariable Long loanId
    ) {
        return ResponseEntity.ok(loanService.getOutstandingBalance(loanId));
    }

    @PostMapping("/close")
    public ResponseEntity<ApiResponse<LoanResponse>> closeLoan(
            @Valid @RequestBody LoanClosureRequest request
    ) {
        return ResponseEntity.ok(loanClosureService.foreclosureLoan(request));
    }
}