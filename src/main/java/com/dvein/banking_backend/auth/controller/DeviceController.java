package com.dvein.banking_backend.auth.controller;

import com.dvein.banking_backend.auth.dto.request.RegisterDeviceRequest;
import com.dvein.banking_backend.auth.dto.response.DeviceResponse;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.auth.service.DeviceService;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
@RequireRole(UserRole.CUSTOMER)
@Tag(name = "Device Management", description = "Device management endpoints")
public class DeviceController {

    private final DeviceService deviceService;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;

    @PostMapping("/register")
    @Operation(summary = "Register device", description = "Register new device for user")
    @RateLimited(limit = 10, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.CREATE, entityType = "Device", description = "Device registered")
    public ResponseEntity<ApiResponse<DeviceResponse>> registerDevice(
            @Valid @RequestBody RegisterDeviceRequest request,
            HttpServletRequest httpRequest) {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DeviceResponse device = deviceService.registerDevice(user.getId(), request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessMessages.DEVICE_REGISTERED, device));
    }

    @GetMapping
    @Operation(summary = "Get all devices", description = "Get all trusted devices for user")
    @RateLimited(limit = 30, duration = 60, keyType = RateLimited.KeyType.USER)
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> getDevices() {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<DeviceResponse> devices = deviceService.getUserDevices(user.getId());
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "Remove device", description = "Remove trusted device")
    @RateLimited(limit = 10, duration = 3600, keyType = RateLimited.KeyType.USER)
    @Audited(action = AuditAction.DELETE, entityType = "Device", description = "Device removed")
    public ResponseEntity<ApiResponse<Void>> removeDevice(@PathVariable Long deviceId) {
        String userEmail = securityContextHelper.getCurrentUserEmail();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        deviceService.removeDevice(user.getId(), deviceId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.DEVICE_REMOVED, null));
    }
}