package com.dvein.banking_backend.auth.controller;

import com.dvein.banking_backend.auth.dto.request.CreateMpinRequest;
import com.dvein.banking_backend.auth.dto.request.VerifyMpinRequest;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.auth.service.MpinService;
import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.annotation.RequireRole;
import com.dvein.banking_backend.common.constant.SuccessMessages;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.auth.dto.request.ChangeMpinRequest;
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
@RequestMapping("/mpin")
@RequiredArgsConstructor
@RequireRole(UserRole.CUSTOMER)
@Tag(name = "MPIN Management", description = "MPIN related endpoints")
public class MpinController {

    private final MpinService mpinService;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;

    @PostMapping("/create")
    @Operation(summary = "Create MPIN", description = "Create new MPIN for quick transactions")
    @RateLimited(limit = 3, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.CREATE, entityType = "MPIN", description = "MPIN created")
    public ResponseEntity<ApiResponse<Void>> createMpin(@Valid @RequestBody CreateMpinRequest request) {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                mpinService.createMpin(user.getId(), request.getMpin(), request.getConfirmMpin());
            }
        }
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.MPIN_CREATED, null));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify MPIN", description = "Verify MPIN for transactions")
    @RateLimited(limit = 10, duration = 300, keyType = RateLimited.KeyType.USER,
            message = "Too many MPIN verification attempts. Please try again later.")
    public ResponseEntity<ApiResponse<Void>> verifyMpin(@Valid @RequestBody VerifyMpinRequest request) {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                mpinService.verifyMpin(user.getId(), request.getMpin());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("MPIN verified successfully", null));
    }

    @PostMapping("/change")
    @Operation(summary = "Change MPIN", description = "Change existing MPIN")
    @RateLimited(limit = 5, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.UPDATE, entityType = "MPIN", description = "MPIN changed")
    public ResponseEntity<ApiResponse<Void>> changeMpin(@Valid @RequestBody ChangeMpinRequest request) {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow();

            mpinService.changeMpin(
                    user.getId(),
                    request.getOldMpin(),
                    request.getNewMpin(),
                    request.getConfirmMpin()
            );
        }
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.MPIN_CHANGED, null));
    }
}