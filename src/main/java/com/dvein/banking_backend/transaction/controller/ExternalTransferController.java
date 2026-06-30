package com.dvein.banking_backend.transaction.controller;

import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.transaction.dto.request.ExternalTransferRequest;
import com.dvein.banking_backend.transaction.dto.response.TransactionResponse;
import com.dvein.banking_backend.transaction.service.ExternalTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions/external")
@RequiredArgsConstructor
@Tag(name = "External Transfer", description = "External bank transfer endpoints (IMPS/NEFT/RTGS)")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('CUSTOMER')")
public class ExternalTransferController {

    private final ExternalTransferService externalTransferService;
    private final SecurityContextHelper securityContextHelper;

    @PostMapping
    @Operation(summary = "External transfer", description = "Transfer money to external bank account (IMPS/NEFT/RTGS)")
    @RateLimited
    @Audited(action = AuditAction.CREATE, entityType = "Transaction", description = "External transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transferMoney(
            @Valid @RequestBody ExternalTransferRequest request,
            HttpServletRequest httpRequest) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        TransactionResponse response = externalTransferService.transferMoney(request, email, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("External transfer initiated successfully. Processing time: 30 seconds.", response));
    }
}