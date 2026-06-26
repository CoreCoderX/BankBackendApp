package com.dvein.banking_backend.loan.controller;

import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.dto.PageResponse;
import com.dvein.banking_backend.loan.dto.request.LoanApprovalRequest;
import com.dvein.banking_backend.loan.dto.response.LoanResponse;
import com.dvein.banking_backend.loan.enums.LoanStatus;
import com.dvein.banking_backend.loan.service.LoanDisbursementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/loan")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class LoanAdminController {

    private final LoanDisbursementService loanDisbursementService;

    @PostMapping("/approve-reject")
    public ResponseEntity<ApiResponse<LoanResponse>> approveOrRejectLoan(
            @Valid @RequestBody LoanApprovalRequest request
    ) {
        return ResponseEntity.ok(loanDisbursementService.approveOrRejectLoan(request));
    }
}