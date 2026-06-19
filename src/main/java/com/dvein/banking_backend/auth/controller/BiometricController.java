package com.dvein.banking_backend.auth.controller;

import com.dvein.banking_backend.auth.dto.request.BiometricToggleRequest;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.auth.service.BiometricService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/biometric")
@RequiredArgsConstructor
@RequireRole(UserRole.CUSTOMER)
@Tag(name = "Biometric Authentication", description = "Biometric authentication endpoints")
public class BiometricController {

    private final BiometricService biometricService;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;

    @PostMapping("/toggle")
    @Operation(summary = "Toggle biometric", description = "Enable or disable biometric authentication")
    @RateLimited(limit = 10, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.UPDATE, entityType = "Biometric", description = "Biometric toggled")
    public ResponseEntity<ApiResponse<Void>> toggleBiometric(
            @Valid @RequestBody BiometricToggleRequest request) {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        biometricService.toggleBiometric(user.getId(), request.getEnable());
        String message = request.getEnable() ?
                "Biometric enabled successfully" :
                "Biometric disabled successfully";

        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    @GetMapping("/status")
    @Operation(summary = "Get biometric status", description = "Check if biometric is enabled")
    @RateLimited(limit = 30, duration = 60, keyType = RateLimited.KeyType.USER)
    public ResponseEntity<ApiResponse<Boolean>> getBiometricStatus() {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean enabled = biometricService.isBiometricEnabled(user.getId());
        return ResponseEntity.ok(ApiResponse.success(enabled));
    }
}