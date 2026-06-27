package com.dvein.banking_backend.FundTransaction.controller;

import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.FundTransaction.dto.request.AccountTransferRequest;
import com.dvein.banking_backend.FundTransaction.dto.request.BeneficiaryTransferRequest;
import com.dvein.banking_backend.FundTransaction.dto.request.SelfTransferRequest;
import com.dvein.banking_backend.FundTransaction.dto.response.TransactionResponse;
import com.dvein.banking_backend.FundTransaction.service.FundTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fund")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class FundTransferController {

    private final FundTransferService fundTransferService;

    @PostMapping("/self-transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> selfTransfer(
            @Valid @RequestBody SelfTransferRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(fundTransferService.selfTransfer(request));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> accountTransfer(
            @Valid @RequestBody AccountTransferRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(fundTransferService.accountTransfer(request));
    }

    @PostMapping("/beneficiary-transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> beneficiaryTransfer(
            @Valid @RequestBody BeneficiaryTransferRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(fundTransferService.beneficiaryTransfer(request));
    }
}