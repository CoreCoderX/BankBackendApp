package com.dvein.banking_backend.common.security;

import com.dvein.banking_backend.common.util.EncryptionUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceFingerprint {

    private final EncryptionUtil encryptionUtil;

    public String generateFingerprint(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String acceptLanguage = request.getHeader("Accept-Language");
        String acceptEncoding = request.getHeader("Accept-Encoding");

        String fingerprint = String.format("%s|%s|%s",
                userAgent != null ? userAgent : "unknown",
                acceptLanguage != null ? acceptLanguage : "unknown",
                acceptEncoding != null ? acceptEncoding : "unknown"
        );

        return encryptionUtil.hashSHA256(fingerprint);
    }

    public String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // If multiple IPs, take the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }

    public String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}