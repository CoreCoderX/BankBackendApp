package com.dvein.banking_backend.transaction.controller;

import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.transaction.dto.response.TransactionStatementResponse;
import com.dvein.banking_backend.transaction.service.TransactionStatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/transactions/statement")
@RequiredArgsConstructor
@Tag(name = "Transaction Statements", description = "Account statement endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('CUSTOMER')")
public class TransactionStatementController {

    private final TransactionStatementService statementService;
    private final SecurityContextHelper securityContextHelper;

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account statement", description = "Get account statement for date range")
    @RateLimited
    public ResponseEntity<ApiResponse<TransactionStatementResponse>> getStatement(
            @PathVariable Long accountId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        TransactionStatementResponse response = statementService.getStatement(accountId, startDate, endDate, email);
        return ResponseEntity.ok(ApiResponse.success("Statement retrieved successfully", response));
    }

    @GetMapping("/{accountId}/download")
    @Operation(summary = "Download statement", description = "Download account statement as CSV")
    @RateLimited
    public ResponseEntity<byte[]> downloadStatement(
            @PathVariable Long accountId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        String csvContent = statementService.downloadStatement(accountId, startDate, endDate, email);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=statement_" + accountId + "_" + startDate + "_to_" + endDate + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvContent.getBytes());
    }

    @PostMapping("/{accountId}/email")
    @Operation(summary = "Email statement", description = "Email account statement to registered email")
    @RateLimited
    public ResponseEntity<ApiResponse<Void>> emailStatement(
            @PathVariable Long accountId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        statementService.emailStatement(accountId, startDate, endDate, email);
        return ResponseEntity.ok(ApiResponse.success("Statement emailed successfully", null));
    }
}