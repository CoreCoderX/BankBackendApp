package com.dvein.banking_backend.admin.dto.response;

import com.dvein.banking_backend.common.enums.AuditAction;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Audit log response")
public class AuditLogResponse {

    @Schema(description = "Log ID", example = "1")
    private Long id;

    @Schema(description = "User ID who performed action", example = "1")
    private Long userId;

    @Schema(description = "User email", example = "admin@banking.com")
    private String userEmail;

    @Schema(description = "Action performed")
    private AuditAction action;

    @Schema(description = "Entity type", example = "Customer")
    private String entityType;

    @Schema(description = "Entity ID", example = "123")
    private Long entityId;

    @Schema(description = "Description", example = "Customer KYC approved")
    private String description;

    @Schema(description = "IP address", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "User agent")
    private String userAgent;

    @Schema(description = "Created at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}