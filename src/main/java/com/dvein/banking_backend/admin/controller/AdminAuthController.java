package com.dvein.banking_backend.admin.controller;

import com.dvein.banking_backend.auth.dto.request.LoginRequest;
import com.dvein.banking_backend.auth.dto.response.LoginResponse;
import com.dvein.banking_backend.auth.service.AuthService;
import com.dvein.banking_backend.common.constant.SuccessMessages;
import com.dvein.banking_backend.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
@Tag(name = "Admin Authentication", description = "Admin login endpoints")
public class AdminAuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Admin login", description = "Admin login with email and password")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        LoginResponse response = authService.adminLogin(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.LOGIN_SUCCESS, response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Admin logout", description = "Admin logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.LOGOUT_SUCCESS, null));
    }
}