package com.dvein.banking_backend.auth.service;

import com.dvein.banking_backend.auth.repository.PreAuthenticationSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationCleanupService {

    private final PreAuthenticationSessionRepository preAuthSessionRepository;

    /**
     * Clean up expired pre-authentication sessions every 10 minutes
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    @Transactional
    public void cleanupExpiredPreAuthSessions() {
        try {
            preAuthSessionRepository.deactivateExpiredSessions(LocalDateTime.now());
            log.info("Expired pre-authentication sessions cleaned up");
        } catch (Exception e) {
            log.error("Error cleaning up expired pre-auth sessions", e);
        }
    }
}