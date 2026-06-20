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
@Schema(description = "Device information response")
public class DeviceResponse {

    @Schema(description = "Device ID", example = "1")
    private Long id;

    @Schema(description = "Device unique identifier", example = "device-uuid-1234")
    private String deviceId;

    @Schema(description = "Device name", example = "Chrome on Windows")
    private String deviceName;

    @Schema(description = "IP address", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "Whether device is trusted", example = "true")
    private boolean trusted;

    @Schema(description = "Whether device is active", example = "true")
    private boolean active;

    @Schema(description = "Registration date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Last used date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUsedAt;
}