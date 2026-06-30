package com.dvein.banking_backend.auth.service;

import com.dvein.banking_backend.auth.dto.response.SessionResponse;
import com.dvein.banking_backend.auth.model.Device;
import com.dvein.banking_backend.auth.model.Session;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.DeviceRepository;
import com.dvein.banking_backend.auth.repository.SessionRepository;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.config.JwtConfig;
import com.dvein.banking_backend.common.exception.CustomException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.security.DeviceFingerprint;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceFingerprint deviceFingerprint;
    private final JwtConfig jwtConfig;
    private final TokenBlacklistService tokenBlacklistService;


    @Value("${security.max-concurrent-sessions:3}")
    private int maxConcurrentSessions;

    @Transactional
    public Session createSession(User user, String refreshToken, String deviceId, HttpServletRequest request) {

        // Check and enforce max concurrent sessions
        List<Session> activeSessions = sessionRepository.findByUserAndActiveTrue(user);

        if (activeSessions.size() >= maxConcurrentSessions) {
            // Find oldest session
            Session oldestSession = activeSessions.stream()
                    .min((s1, s2) -> s1.getCreatedAt().compareTo(s2.getCreatedAt()))
                    .orElse(null);

            if (oldestSession != null) {
                // Invalidate oldest session
                oldestSession.invalidate();
                sessionRepository.save(oldestSession);

                // Blacklist its refresh token
                tokenBlacklistService.blacklistToken(
                        oldestSession.getRefreshToken(),
                        user.getId(),
                        "MAX_CONCURRENT_SESSIONS_EXCEEDED"
                );

                log.warn("Oldest session force-closed for user: {} due to max session limit", user.getEmail());
            }
        }

        // Find device if provided
        Device device = null;
        if (deviceId != null) {
            device = deviceRepository.findByDeviceId(deviceId).orElse(null);
        }

        // Calculate expiry time
        LocalDateTime expiryTime = LocalDateTime.now()
                .plusSeconds(jwtConfig.getRefreshTokenExpiry() / 1000);

        // Create new session
        Session session = Session.builder()
                .user(user)
                .refreshToken(refreshToken)
                .device(device)
                .ipAddress(deviceFingerprint.getIpAddress(request))
                .userAgent(deviceFingerprint.getUserAgent(request))
                .expiresAt(expiryTime)
                .lastActivityAt(LocalDateTime.now())
                .build();

        session = sessionRepository.save(session);

        log.info("Session created for user: {} - Session ID: {} - Total active: {}",
                user.getEmail(), session.getId(), activeSessions.size() + 1);

        return session;
    }

    public List<SessionResponse> getActiveSessions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Session> sessions = sessionRepository.findByUserAndActiveTrue(user);

        return sessions.stream()
                .map(this::mapToSessionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void logoutSession(Long sessionId, Long userId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));

        if (!session.getUser().getId().equals(userId)) {
            throw new CustomException("Session does not belong to user", "SESSION_001");
        }

        // Invalidate session
        session.invalidate();
        sessionRepository.save(session);

        // Blacklist the refresh token
        tokenBlacklistService.blacklistToken(
                session.getRefreshToken(),
                userId,
                "USER_LOGOUT"
        );

        log.info("Session logged out - Session ID: {} for User ID: {}", sessionId, userId);
    }

    @Transactional
    public void logoutAllSessions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Session> activeSessions = sessionRepository.findByUserAndActiveTrue(user);

        for (Session session : activeSessions) {
            session.invalidate();

            // Blacklist each refresh token
            tokenBlacklistService.blacklistToken(
                    session.getRefreshToken(),
                    userId,
                    "LOGOUT_ALL_SESSIONS"
            );
        }

        sessionRepository.saveAll(activeSessions);

        log.info("All {} sessions logged out for user: {}", activeSessions.size(), user.getEmail());
    }

    @Transactional
    public void logoutOtherSessions(Long userId, Long currentSessionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Session> otherSessions = sessionRepository.findByUserAndActiveTrue(user).stream()
                .filter(session -> !session.getId().equals(currentSessionId))
                .collect(Collectors.toList());

        for (Session session : otherSessions) {
            session.invalidate();

            // Blacklist each refresh token
            tokenBlacklistService.blacklistToken(
                    session.getRefreshToken(),
                    userId,
                    "LOGOUT_OTHER_SESSIONS"
            );
        }

        sessionRepository.saveAll(otherSessions);

        log.info("{} other sessions logged out for user: {}", otherSessions.size(), user.getEmail());
    }

    @Transactional
    public void updateSessionActivity(String refreshToken) {
        sessionRepository.findByRefreshToken(refreshToken).ifPresent(session -> {
            if (session.isActive() && !session.isExpired()) {
                session.updateActivity();
                sessionRepository.save(session);
            }
        });
    }

    public boolean isSessionValid(String refreshToken) {
        // Check if token is blacklisted
        if (tokenBlacklistService.isTokenBlacklisted(refreshToken)) {
            return false;
        }

        return sessionRepository.findByRefreshToken(refreshToken)
                .map(session -> session.isActive() && !session.isExpired())
                .orElse(false);
    }

    @Transactional
    @Scheduled(cron = "0 0 */2 * * *") // Run every 2 hours
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();

        // FIX: Use the new scoped query instead of findByUserAndActiveTrue(null)
        // The old query passed null as the User param, which always returned empty results
        // (WHERE user = NULL AND active = true never matches any rows).
        List<Session> expiredActiveSessions = sessionRepository.findExpiredActiveSessions(now);

        // Invalidate and blacklist tokens for genuinely active-but-expired sessions
        for (Session session : expiredActiveSessions) {
            session.invalidate();

            try {
                tokenBlacklistService.blacklistToken(
                        session.getRefreshToken(),
                        session.getUser().getId(),
                        "SESSION_EXPIRED"
                );
            } catch (Exception e) {
                log.warn("Could not blacklist token for expired session: {}", session.getId(), e);
            }
        }

        if (!expiredActiveSessions.isEmpty()) {
            sessionRepository.saveAll(expiredActiveSessions);
            log.info("Invalidated {} expired sessions", expiredActiveSessions.size());
        }

        // Hard-delete sessions that are either expired or inactive and older than 30 days
        sessionRepository.deleteExpiredAndInactiveSessions(now.minusDays(30));

        log.info("Session cleanup complete");
    }

    private SessionResponse mapToSessionResponse(Session session) {
        return SessionResponse.builder()
                .id(session.getId())
                .deviceName(session.getDevice() != null ? session.getDevice().getDeviceName() : "Unknown Device")
                .ipAddress(session.getIpAddress())
                // FIX: Include userAgent so the SessionController can compare it against
                // the current request's User-Agent to correctly set the 'current' flag.
                .userAgent(session.getUserAgent())
                .active(session.isActive())
                .createdAt(session.getCreatedAt())
                .lastActivityAt(session.getLastActivityAt())
                .expiresAt(session.getExpiresAt())
                .current(false) // Will be set by the controller after comparison
                .build();
    }
}