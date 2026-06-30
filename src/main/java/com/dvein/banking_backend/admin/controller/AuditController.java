package com.dvein.banking_backend.admin.controller;

import com.dvein.banking_backend.admin.dto.response.AuditLogResponse;
import com.dvein.banking_backend.admin.service.AuditService;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.enums.AuditAction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@Tag(name = "Audit Logs", description = "Audit log management endpoints")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Get audit logs", description = "Get all audit logs with pagination")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(
                sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy
        );
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AuditLogResponse> logs = auditService.getAuditLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get audit logs by user", description = "Get audit logs for specific user")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogResponse> logs = auditService.getAuditLogsByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/action/{action}")
    @Operation(summary = "Get audit logs by action", description = "Get audit logs for specific action type")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogsByAction(
            @PathVariable AuditAction action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogResponse> logs = auditService.getAuditLogsByAction(action, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}