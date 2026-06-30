package com.dvein.banking_backend.admin.dto.response;

import com.dvein.banking_backend.common.enums.UserRole;
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
@Schema(description = "Admin profile response")
public class AdminProfileResponse {

    @Schema(description = "Admin ID", example = "1")
    private Long id;

    @Schema(description = "Email", example = "admin@banking.com")
    private String email;

    @Schema(description = "Role")
    private UserRole role;

    @Schema(description = "Active status", example = "true")
    private boolean active;

    @Schema(description = "Created at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Last login at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;
}