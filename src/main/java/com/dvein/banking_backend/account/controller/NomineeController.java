package com.dvein.banking_backend.account.controller;

import com.dvein.banking_backend.account.dto.request.AddNomineeRequest;
import com.dvein.banking_backend.account.dto.response.NomineeResponse;
import com.dvein.banking_backend.account.service.NomineeService;
import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.annotation.RequireRole;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.common.enums.UserRole;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nominee")
@RequiredArgsConstructor
@RequireRole(UserRole.CUSTOMER)
@Tag(name = "Nominee Management", description = "Nominee management endpoints")
public class NomineeController {

    private final NomineeService nomineeService;
    private final SecurityContextHelper securityContextHelper;

    @PostMapping("/{accountId}")
    @Operation(summary = "Add nominee", description = "Add new nominee to account")
    @RateLimited(limit = 20, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.CREATE, entityType = "Nominee", description = "Nominee added")
    public ResponseEntity<ApiResponse<NomineeResponse>> addNominee(
            @PathVariable Long accountId,
            @Valid @RequestBody AddNomineeRequest request) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        NomineeResponse nominee = nomineeService.addNominee(accountId, email, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Nominee added successfully", nominee));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get nominees", description = "Get all nominees for account")
    @RateLimited(limit = 30, duration = 60, keyType = RateLimited.KeyType.USER)
    public ResponseEntity<ApiResponse<List<NomineeResponse>>> getNominees(
            @PathVariable Long accountId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        List<NomineeResponse> nominees = nomineeService.getAccountNominees(accountId, email);
        return ResponseEntity.ok(ApiResponse.success(nominees));
    }

    @DeleteMapping("/{accountId}/{nomineeId}")
    @Operation(summary = "Remove nominee", description = "Remove nominee from account")
    @RateLimited(limit = 10, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.DELETE, entityType = "Nominee", description = "Nominee removed")
    public ResponseEntity<ApiResponse<Void>> removeNominee(
            @PathVariable Long accountId,
            @PathVariable Long nomineeId) {
        String email = securityContextHelper.getCurrentUserEmailOrThrow();
        nomineeService.removeNominee(accountId, nomineeId, email);
        return ResponseEntity.ok(ApiResponse.success("Nominee removed successfully", null));
    }
}