package com.dvein.banking_backend.auth.service;

import com.dvein.banking_backend.auth.dto.request.RegisterDeviceRequest;
import com.dvein.banking_backend.auth.dto.response.DeviceResponse;
import com.dvein.banking_backend.auth.model.Device;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.DeviceRepository;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.exception.CustomException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.security.DeviceFingerprint;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final DeviceFingerprint deviceFingerprint;

    private static final int MAX_DEVICES = 5;

    @Transactional
    public DeviceResponse registerDevice(Long userId, RegisterDeviceRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if device already exists
        if (deviceRepository.existsByDeviceId(request.getDeviceId())) {
            throw new CustomException("Device already registered", "DEV_002");
        }

        // Check device limit
        long deviceCount = deviceRepository.countByUserAndActiveTrue(user);
        if (deviceCount >= MAX_DEVICES) {
            throw new CustomException("Maximum device limit reached", "DEV_003");
        }

        // Create new device
        Device device = Device.builder()
                .user(user)
                .deviceId(request.getDeviceId())
                .deviceName(request.getDeviceName())
                .deviceFingerprint(deviceFingerprint.generateFingerprint(httpRequest))
                .userAgent(deviceFingerprint.getUserAgent(httpRequest))
                .ipAddress(deviceFingerprint.getIpAddress(httpRequest))
                .trusted(request.getTrusted())
                .lastUsedAt(LocalDateTime.now())
                .build();

        device = deviceRepository.save(device);

        log.info("Device registered for user: {} - Device: {}", user.getEmail(), request.getDeviceName());

        return mapToDeviceResponse(device);
    }

    public List<DeviceResponse> getUserDevices(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Device> devices = deviceRepository.findByUser(user);

        return devices.stream()
                .map(this::mapToDeviceResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeDevice(Long userId, Long deviceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));

        if (!device.getUser().getId().equals(userId)) {
            throw new CustomException("Device does not belong to user", "DEV_001");
        }

        device.setActive(false);
        deviceRepository.save(device);

        log.info("Device removed for user: {} - Device ID: {}", user.getEmail(), deviceId);
    }

    @Transactional
    public void updateDeviceActivity(String deviceId, HttpServletRequest request) {
        deviceRepository.findByDeviceId(deviceId).ifPresent(device -> {
            device.setLastUsedAt(LocalDateTime.now());
            device.setIpAddress(deviceFingerprint.getIpAddress(request));
            deviceRepository.save(device);
        });
    }

    @Transactional
    public void updateDeviceTrustStatus(String deviceId, boolean trusted) {
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "deviceId", deviceId));

        device.setTrusted(trusted);
        deviceRepository.save(device);

        log.info("Device trust status updated: {} - Trusted: {}", deviceId, trusted);
    }

    public boolean isDeviceTrusted(Long userId, String deviceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return deviceRepository.findByUserAndDeviceId(user, deviceId)
                .map(Device::isTrusted)
                .orElse(false);
    }

    public boolean deviceExists(String deviceId) {
        return deviceRepository.existsByDeviceId(deviceId);
    }

    private DeviceResponse mapToDeviceResponse(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .deviceId(device.getDeviceId())
                .deviceName(device.getDeviceName())
                .ipAddress(device.getIpAddress())
                .trusted(device.isTrusted())
                .active(device.isActive())
                .createdAt(device.getCreatedAt())
                .lastUsedAt(device.getLastUsedAt())
                .build();
    }
}