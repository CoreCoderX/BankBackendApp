package com.dvein.banking_backend.auth.controller;

import com.dvein.banking_backend.auth.dto.response.SessionResponse;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.auth.service.SessionService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@RequireRole(UserRole.CUSTOMER)
@Tag(name = "Session Management", description = "Session management endpoints")
public class SessionController {

    private final SessionService sessionService;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get active sessions", description = "Get all active sessions for user")
    @RateLimited(limit = 30, duration = 60, keyType = RateLimited.KeyType.USER)
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getActiveSessions() {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SessionResponse> sessions = sessionService.getActiveSessions(user.getId());
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @PostMapping("/{sessionId}/logout")
    @Operation(summary = "Logout session", description = "Logout specific session")
    @RateLimited(limit = 10, duration = 60, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.LOGOUT, entityType = "Session", description = "Session logged out")
    public ResponseEntity<ApiResponse<Void>> logoutSession(@PathVariable Long sessionId) {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        sessionService.logoutSession(sessionId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.LOGOUT_SUCCESS, null));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout all sessions", description = "Logout all active sessions")
    @RateLimited(limit = 5, duration = 300, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.LOGOUT, entityType = "Session", description = "All sessions logged out")
    public ResponseEntity<ApiResponse<Void>> logoutAllSessions() {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        sessionService.logoutAllSessions(user.getId());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.LOGOUT_SUCCESS, null));
    }
}