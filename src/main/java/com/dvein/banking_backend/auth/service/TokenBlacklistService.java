package com.dvein.banking_backend.auth.service;

import com.dvein.banking_backend.auth.model.TokenBlacklist;
import com.dvein.banking_backend.auth.repository.TokenBlacklistRepository;
import com.dvein.banking_backend.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void blacklistToken(String token, Long userId, String reason) {
        try {
            Date expiration = jwtTokenProvider.extractExpiration(token);
            LocalDateTime expiresAt = expiration.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            if (LocalDateTime.now().isAfter(expiresAt)) {
                return;
            }

            TokenBlacklist blacklistedToken = TokenBlacklist.builder()
                    .token(token)
                    .userId(userId)
                    .reason(reason)
                    .expiresAt(expiresAt)
                    .build();

            tokenBlacklistRepository.save(blacklistedToken);
            log.info("Token blacklisted - User: {} - Reason: {}", userId, reason);
        } catch (Exception e) {
            log.error("Failed to blacklist token: {}", e.getMessage());
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }

    @Scheduled(cron = "0 0 */6 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up {} expired tokens", deleted);
    }
}