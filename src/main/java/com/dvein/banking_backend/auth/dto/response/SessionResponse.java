package com.dvein.banking_backend.auth.dto.response;

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
@Schema(description = "Session information response")
public class SessionResponse {

    @Schema(description = "Session ID", example = "1")
    private Long id;

    @Schema(description = "Device name", example = "Chrome on Windows")
    private String deviceName;

    @Schema(description = "IP address", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "Whether session is active", example = "true")
    private boolean active;

    @Schema(description = "Session created date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Last activity date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActivityAt;

    @Schema(description = "Session expiry date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    @Schema(description = "Whether this is the current session", example = "true")
    @Builder.Default
    private boolean current = false;
}