package com.dvein.banking_backend.auth.controller;

import com.dvein.banking_backend.auth.dto.request.EnableTotpRequest;
import com.dvein.banking_backend.auth.dto.request.VerifyTotpRequest;
import com.dvein.banking_backend.auth.dto.response.TotpSetupResponse;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.auth.service.TotpService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/totp")
@RequiredArgsConstructor
@RequireRole(UserRole.CUSTOMER)
@Tag(name = "Two-Factor Authentication", description = "TOTP (Google Authenticator) related endpoints")
public class TotpController {

    private final TotpService totpService;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;

    @PostMapping("/setup")
    @Operation(summary = "Setup TOTP", description = "Generate QR code for TOTP setup")
    @RateLimited(limit = 5, duration = 3600, keyType = RateLimited.KeyType.USER)
    public ResponseEntity<ApiResponse<TotpSetupResponse>> setupTotp() {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TotpSetupResponse setupResponse = totpService.setupTotp(user.getId());
        return ResponseEntity.ok(ApiResponse.success("TOTP setup initiated", setupResponse));
    }

    @PostMapping("/enable")
    @Operation(summary = "Enable TOTP", description = "Enable TOTP using verification code")
    @RateLimited(limit = 5, duration = 600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.UPDATE, entityType = "TOTP", description = "TOTP enabled")
    public ResponseEntity<ApiResponse<Void>> enableTotp(@Valid @RequestBody EnableTotpRequest request) {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        totpService.enableTotp(user.getId(), request.getCode());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TOTP_ENABLED, null));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify TOTP", description = "Verify TOTP code during login")
    @RateLimited(limit = 10, duration = 300, keyType = RateLimited.KeyType.USER)
    public ResponseEntity<ApiResponse<Void>> verifyTotp(@Valid @RequestBody VerifyTotpRequest request) {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean valid = totpService.verifyTotp(user.getId(), request.getCode());
        if (!valid) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid TOTP code"));
        }
        return ResponseEntity.ok(ApiResponse.success("TOTP verified successfully", null));
    }

    @PostMapping("/disable")
    @Operation(summary = "Disable TOTP", description = "Disable TOTP verification")
    @RateLimited(limit = 3, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.UPDATE, entityType = "TOTP", description = "TOTP disabled")
    public ResponseEntity<ApiResponse<Void>> disableTotp(@Valid @RequestBody VerifyTotpRequest request) {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        totpService.disableTotp(user.getId(), request.getCode());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TOTP_DISABLED, null));
    }
}