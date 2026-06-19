package com.dvein.banking_backend.auth.service;

import com.dvein.banking_backend.auth.dto.response.LoginHistoryResponse;
import com.dvein.banking_backend.auth.model.LoginHistory;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.LoginHistoryRepository;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.security.DeviceFingerprint;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;
    private final DeviceFingerprint deviceFingerprint;

    @Transactional
    public void recordLoginAttempt(User user, HttpServletRequest request, boolean successful, String failureReason) {
        String userAgent = deviceFingerprint.getUserAgent(request);
        String deviceType = determineDeviceType(userAgent);

        if (user == null) {
            return;
        }

        LoginHistory loginHistory = LoginHistory.builder()
                .user(user)
                .ipAddress(deviceFingerprint.getIpAddress(request))
                .userAgent(userAgent)
                .deviceType(deviceType)
                .location(null) // Can be enhanced with IP geolocation service
                .successful(successful)
                .failureReason(failureReason)
                .build();

        loginHistoryRepository.save(loginHistory);

        log.info("Login attempt recorded for user: {} - Success: {}", user.getEmail(), successful);
    }

    public Page<LoginHistoryResponse> getLoginHistory(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Page<LoginHistory> loginHistoryPage = loginHistoryRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return loginHistoryPage.map(this::mapToLoginHistoryResponse);
    }

    public List<LoginHistoryResponse> getRecentLoginHistory(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<LoginHistory> loginHistories = loginHistoryRepository.findTop10ByUserOrderByCreatedAtDesc(user);

        return loginHistories.stream()
                .limit(limit)
                .map(this::mapToLoginHistoryResponse)
                .collect(Collectors.toList());
    }

    private String determineDeviceType(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }

        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            return "Mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }

    private LoginHistoryResponse mapToLoginHistoryResponse(LoginHistory loginHistory) {
        return LoginHistoryResponse.builder()
                .id(loginHistory.getId())
                .ipAddress(loginHistory.getIpAddress())
                .userAgent(loginHistory.getUserAgent())
                .location(loginHistory.getLocation())
                .deviceType(loginHistory.getDeviceType())
                .successful(loginHistory.isSuccessful())
                .failureReason(loginHistory.getFailureReason())
                .createdAt(loginHistory.getCreatedAt())
                .build();
    }
}