package com.dvein.banking_backend.auth.service;

import com.dvein.banking_backend.auth.dto.response.SessionResponse;
import com.dvein.banking_backend.auth.model.Device;
import com.dvein.banking_backend.auth.model.Session;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.DeviceRepository;
import com.dvein.banking_backend.auth.repository.SessionRepository;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.config.JwtConfig;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.security.DeviceFingerprint;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public Session createSession(User user, String refreshToken, String deviceId, HttpServletRequest request) {
        Device device = null;
        if (deviceId != null) {
            device = deviceRepository.findByDeviceId(deviceId).orElse(null);
        }

        LocalDateTime expiryTime = LocalDateTime.now()
                .plusSeconds(jwtConfig.getRefreshTokenExpiry() / 1000);

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

        log.info("Session created for user: {} - Session ID: {}", user.getEmail(), session.getId());

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
            throw new ResourceNotFoundException("Session not found for user");
        }

        session.invalidate();
        sessionRepository.save(session);

        log.info("Session logged out - Session ID: {}", sessionId);
    }

    @Transactional
    public void logoutAllSessions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        sessionRepository.invalidateAllUserSessions(user);

        log.info("All sessions logged out for user: {}", user.getEmail());
    }

    @Transactional
    public void logoutOtherSessions(Long userId, Long currentSessionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        sessionRepository.invalidateOtherUserSessions(user, currentSessionId);

        log.info("Other sessions logged out for user: {}", user.getEmail());
    }

    @Transactional
    public void updateSessionActivity(String refreshToken) {
        sessionRepository.findByRefreshToken(refreshToken).ifPresent(session -> {
            session.updateActivity();
            sessionRepository.save(session);
        });
    }

    @Transactional
    public boolean isSessionValid(String refreshToken) {
        return sessionRepository.findByRefreshToken(refreshToken)
                .map(session -> session.isActive() && !session.isExpired())
                .orElse(false);
    }

    @Transactional
    @Scheduled(cron = "0 0 */6 * * *") // Run every 6 hours
    public void cleanupExpiredSessions() {
        sessionRepository.deleteExpiredAndInactiveSessions(LocalDateTime.now());
        log.info("Cleaned up expired and inactive sessions");
    }

    private SessionResponse mapToSessionResponse(Session session) {
        return SessionResponse.builder()
                .id(session.getId())
                .deviceName(session.getDevice() != null ? session.getDevice().getDeviceName() : "Unknown Device")
                .ipAddress(session.getIpAddress())
                .active(session.isActive())
                .createdAt(session.getCreatedAt())
                .lastActivityAt(session.getLastActivityAt())
                .expiresAt(session.getExpiresAt())
                .build();
    }
}