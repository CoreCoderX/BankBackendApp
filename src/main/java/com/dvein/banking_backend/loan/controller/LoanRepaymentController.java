package com.dvein.banking_backend.loan.controller;

import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.dto.PageResponse;
import com.dvein.banking_backend.loan.dto.request.LoanRepaymentRequest;
import com.dvein.banking_backend.loan.dto.response.LoanRepaymentResponse;
import com.dvein.banking_backend.loan.service.LoanRepaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loan")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class LoanRepaymentController {

    private final LoanRepaymentService loanRepaymentService;

    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<LoanRepaymentResponse>> makeRepayment(
            @Valid @RequestBody LoanRepaymentRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(loanRepaymentService.makeRepayment(request));
    }

    @GetMapping("/{loanId}/history")
    public ResponseEntity<ApiResponse<PageResponse<LoanRepaymentResponse>>> getRepaymentHistory(
            @PathVariable Long loanId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(loanRepaymentService.getRepaymentHistory(loanId, page, size));
    }
}