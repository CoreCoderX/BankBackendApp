package com.dvein.banking_backend.auth.controller;

import com.dvein.banking_backend.auth.dto.request.*;
import com.dvein.banking_backend.auth.dto.response.LoginResponse;
import com.dvein.banking_backend.auth.dto.response.RegisterResponse;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.auth.service.AuthService;
import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.constant.SuccessMessages;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication related endpoints")
public class AuthController {

    private final AuthService authService;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;

    @PostMapping("/register")
    @Operation(summary = "Register new customer", description = "Create new customer account and send verification OTP")
    @RateLimited(limit = 10, duration = 3600, keyType = RateLimited.KeyType.IP,
            message = "Too many registration attempts. Please try again in an hour.")
    @Audited(action = AuditAction.CREATE, entityType = "User", description = "New user registration")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessMessages.REGISTRATION_SUCCESS, response));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email with OTP", description = "Verify email address using OTP sent during registration")
    @RateLimited(limit = 5, duration = 300, keyType = RateLimited.KeyType.IP)
    @Audited(action = AuditAction.UPDATE, entityType = "User", description = "Email verified")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyEmail(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.EMAIL_VERIFIED, null));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP", description = "Resend OTP to registered email")
    @RateLimited(limit = 3, duration = 300, keyType = RateLimited.KeyType.IP,
            message = "Too many OTP requests. Please wait 5 minutes.")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.resendOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.OTP_SENT, null));
    }

    @PostMapping("/login")
    @Operation(summary = "Customer login", description = "Login with email/phone and password")
    @RateLimited(limit = 5, duration = 300, keyType = RateLimited.KeyType.IP,
            message = "Too many login attempts. Please try again in 5 minutes.")
    @Audited(action = AuditAction.LOGIN, entityType = "User", description = "User login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.LOGIN_SUCCESS, loginResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout current session")
    @Audited(action = AuditAction.LOGOUT, entityType = "User", description = "User logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestParam(required = false) Long sessionId,
            HttpServletRequest request) {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        if (userEmail != null && sessionId != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                authService.logout(user.getId(), sessionId);
            }
        }
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.LOGOUT_SUCCESS, null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Request password reset OTP")
    @RateLimited(limit = 3, duration = 600, keyType = RateLimited.KeyType.IP)
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.OTP_SENT, null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using OTP")
    @RateLimited(limit = 5, duration = 600, keyType = RateLimited.KeyType.IP)
    @Audited(action = AuditAction.PASSWORD_CHANGE, entityType = "User", description = "Password reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.PASSWORD_RESET, null));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change password after login")
    @RateLimited(limit = 5, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.PASSWORD_CHANGE, entityType = "User", description = "Password changed")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                authService.changePassword(user.getId(), request);
            }
        }
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.PASSWORD_CHANGED, null));
    }
}