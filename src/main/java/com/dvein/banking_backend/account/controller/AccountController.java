package com.dvein.banking_backend.account.controller;

import com.dvein.banking_backend.account.dto.request.CreateAccountRequest;
import com.dvein.banking_backend.account.dto.response.AccountResponse;
import com.dvein.banking_backend.account.service.AccountService;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.annotation.RequireRole;
import com.dvein.banking_backend.common.constant.SuccessMessages;
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
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.repository.CustomerRepository;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@RequireRole(UserRole.CUSTOMER)
@Tag(name = "Account Management", description = "Bank account management endpoints")
public class AccountController {

    private final AccountService accountService;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @PostMapping
    @Operation(summary = "Create account", description = "Create new bank account")
    @RateLimited(limit = 5, duration = 3600, keyType = RateLimited.KeyType.USER,
            message = "Account creation limit reached. Please contact support.")
    @Audited(action = AuditAction.CREATE, entityType = "Account", description = "Account created")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        List<AccountResponse> accounts = accountService.getCustomerAccounts(customer.getId());

        AccountResponse account = accountService.createAccount(customer.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessMessages.ACCOUNT_CREATED, account));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account details", description = "Get specific account details")
    @RateLimited(limit = 30, duration = 60, keyType = RateLimited.KeyType.USER)
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(@PathVariable Long accountId) {
        AccountResponse account = accountService.getAccount(accountId);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @GetMapping
    @Operation(summary = "Get all accounts", description = "Get all accounts for customer")
    @RateLimited(limit = 30, duration = 60, keyType = RateLimited.KeyType.USER)
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccounts() {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        List<AccountResponse> accounts = accountService.getCustomerAccounts(customer.getId());
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @PostMapping("/{accountId}/set-primary")
    @Operation(summary = "Set primary account", description = "Set account as primary")
    @RateLimited(limit = 10, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.UPDATE, entityType = "Account", description = "Primary account changed")
    public ResponseEntity<ApiResponse<Void>> setPrimaryAccount(@PathVariable Long accountId) {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        accountService.setPrimaryAccount(customer.getId(), accountId);
        return ResponseEntity.ok(ApiResponse.success("Primary account set successfully", null));
    }

    @PostMapping("/{accountId}/close")
    @Operation(summary = "Close account", description = "Close bank account")
    @RateLimited(limit = 3, duration = 86400, keyType = RateLimited.KeyType.USER,
            message = "Account closure limit reached. Please contact support.")
    @Audited(action = AuditAction.DELETE, entityType = "Account", description = "Account closed")
    public ResponseEntity<ApiResponse<Void>> closeAccount(
            @PathVariable Long accountId,
            @RequestParam String reason) {
        accountService.closeAccount(accountId, reason);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.ACCOUNT_CLOSED, null));
    }
}