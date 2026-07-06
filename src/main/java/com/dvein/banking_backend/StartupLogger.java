package com.dvein.banking_backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Slf4j
@Component
public class StartupLogger {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${server.port}")
    private String port;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("Application Name : {}", appName);
        log.info("Swagger UI       : http://localhost:{}{}/swagger-ui/index.html", port, contextPath);
        log.info("API Docs         : http://localhost:{}{}/api-docs", port, contextPath);
    }
}