package com.dvein.banking_backend.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cookie")
public class CookieConfig {
    private Name name;
    private Boolean secure;
    private Boolean httpOnly;
    private String sameSite;
    private String domain;
    private String path;
    private Integer maxAge;

    @Data
    public static class Name {
        private String refreshToken;
        private String deviceId;
    }
}