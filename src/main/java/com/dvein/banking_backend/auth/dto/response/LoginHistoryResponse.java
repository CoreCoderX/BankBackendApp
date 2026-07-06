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
@Schema(description = "Login history response")
public class LoginHistoryResponse {

    @Schema(description = "History ID", example = "1")
    private Long id;

    @Schema(description = "IP address", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "User agent")
    private String userAgent;

    @Schema(description = "Location", example = "Mumbai, India")
    private String location;

    @Schema(description = "Device type", example = "Desktop")
    private String deviceType;

    @Schema(description = "Whether login was successful", example = "true")
    private boolean successful;

    @Schema(description = "Failure reason if unsuccessful")
    private String failureReason;

    @Schema(description = "Login date and time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}