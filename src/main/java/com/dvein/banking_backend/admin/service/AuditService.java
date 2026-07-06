package com.dvein.banking_backend.admin.service;

import com.dvein.banking_backend.admin.dto.response.AuditLogResponse;
import com.dvein.banking_backend.admin.model.AuditLog;
import com.dvein.banking_backend.admin.repository.AuditLogRepository;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.enums.AuditAction;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void log(Long userId, AuditAction action, String entityType, Long entityId,
                    String description, HttpServletRequest request) {

        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        auditLogRepository.save(auditLog);

        log.debug("Audit log created: {} - {}", action, description);
    }

    public Page<AuditLogResponse> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(this::mapToAuditLogResponse);
    }

    public Page<AuditLogResponse> getAuditLogsByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable)
                .map(this::mapToAuditLogResponse);
    }

    public Page<AuditLogResponse> getAuditLogsByAction(AuditAction action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable)
                .map(this::mapToAuditLogResponse);
    }

    private AuditLogResponse mapToAuditLogResponse(AuditLog auditLog) {
        String userEmail = userRepository.findById(auditLog.getUserId())
                .map(User::getEmail)
                .orElse("Unknown");

        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .userEmail(userEmail)
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .description(auditLog.getDescription())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}