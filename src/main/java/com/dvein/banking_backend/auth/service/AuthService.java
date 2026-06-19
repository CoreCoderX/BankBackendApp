package com.dvein.banking_backend.auth.service;

import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.auth.dto.request.*;
import com.dvein.banking_backend.auth.dto.response.LoginResponse;
import com.dvein.banking_backend.auth.dto.response.RegisterResponse;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.account.repository.CustomerRepository;
import com.dvein.banking_backend.common.constant.ErrorCodes;
import com.dvein.banking_backend.common.constant.SuccessMessages;
import com.dvein.banking_backend.common.enums.CustomerStatus;
import com.dvein.banking_backend.common.enums.OtpType;
import com.dvein.banking_backend.common.enums.UserRole;
import com.dvein.banking_backend.common.exception.*;
import com.dvein.banking_backend.common.security.JwtTokenProvider;
import com.dvein.banking_backend.common.util.EncryptionUtil;
import com.dvein.banking_backend.common.util.PasswordValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final TotpService totpService;
    private final DeviceService deviceService;
    private final SessionService sessionService;
    private final LoginHistoryService loginHistoryService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EncryptionUtil encryptionUtil;
    private final PasswordValidator passwordValidator;
    private final CustomerRepository customerRepository;

    @Value("${security.max-login-attempts}")
    private int maxLoginAttempts;

    @Value("${security.account-lock-duration}")
    private long lockDuration;

    // ==================== REGISTRATION ====================

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email");
        }

        // Validate phone uniqueness
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("User", "phone");
        }

        // Validate password strength
        PasswordValidator.ValidationResult passwordValidation =
                passwordValidator.validate(request.getPassword());

        if (!passwordValidation.isValid()) {
            throw new InvalidRequestException(
                    "Password validation failed: "
                            + passwordValidation.getErrorMessage());
        }

        // Hash password
        String hashedPassword =
                encryptionUtil.hashPassword(request.getPassword());

        // Create User
        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(hashedPassword)
                .role(UserRole.CUSTOMER)
                .active(false)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        // Create Customer Profile
        Customer customer = Customer.builder()
                .user(user)
                .firstName("New")
                .lastName("Customer")
                .status(CustomerStatus.ACTIVE)
                .build();

        customerRepository.save(customer);

        // Send verification OTP
        otpService.generateAndSendOtp(
                user.getEmail(),
                OtpType.EMAIL_VERIFICATION
        );

        log.info("User registered successfully: {}", user.getEmail());

        return RegisterResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .message(SuccessMessages.REGISTRATION_SUCCESS)
                .requiresEmailVerification(true)
                .build();
    }

    @Transactional
    public void verifyEmail(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Verify OTP
        otpService.verifyOtp(email, otp, OtpType.EMAIL_VERIFICATION);

        // Mark user as active and email verified
        user.setActive(true);
        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("Email verified for user: {}", email);
    }

    @Transactional
    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        otpService.generateAndSendOtp(email, OtpType.EMAIL_VERIFICATION);

        log.info("OTP resent for user: {}", email);
    }

    // ==================== LOGIN ====================

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // Find user by email or phone
        User user = userRepository.findByEmailOrPhone(request.getIdentifier())
                .orElseThrow(() -> {
                    try {
                        loginHistoryService.recordLoginAttempt(null, httpRequest, false, ErrorCodes.AUTH_001);
                    } catch (Exception e) {
                        // Ignore if recording fails
                    }
                    return new UnauthorizedException(ErrorCodes.AUTH_001);
                });

        // Check if account is locked
        if (user.isAccountLocked()) {
            loginHistoryService.recordLoginAttempt(user, httpRequest, false, ErrorCodes.AUTH_002);
            throw new AccountLockedException();
        }

        // Check if user is active
        if (!user.isActive()) {
            loginHistoryService.recordLoginAttempt(user, httpRequest, false, ErrorCodes.AUTH_007);
            throw new CustomException(ErrorCodes.AUTH_007, "AUTH_007");
        }

        // Check if email is verified
        if (!user.isEmailVerified()) {
            loginHistoryService.recordLoginAttempt(user, httpRequest, false, ErrorCodes.AUTH_008);
            throw new CustomException(ErrorCodes.AUTH_008, "AUTH_008");
        }
        // Check customer status
        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer",
                        "userId",
                        user.getId()
                ));

        if (customer.getStatus() != CustomerStatus.ACTIVE) {

            loginHistoryService.recordLoginAttempt(
                    user,
                    httpRequest,
                    false,
                    "ACCOUNT_" + customer.getStatus()
            );

            throw new InvalidRequestException(
                    "Account is " + customer.getStatus()
            );
        }

        // Authenticate using Spring Security
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
            );

            // Reset failed login attempts
            user.resetFailedAttempts();
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Record successful login
            loginHistoryService.recordLoginAttempt(user, httpRequest, true, null);

            // Register device if provided
            if (request.getDeviceId() != null && request.getDeviceName() != null) {
                if (!deviceService.deviceExists(request.getDeviceId())) {
                    RegisterDeviceRequest deviceRequest = RegisterDeviceRequest.builder()
                            .deviceId(request.getDeviceId())
                            .deviceName(request.getDeviceName())
                            .trusted(false)
                            .build();
                    deviceService.registerDevice(user.getId(), deviceRequest, httpRequest);
                }
                deviceService.updateDeviceActivity(request.getDeviceId(), httpRequest);
            }

            // Generate tokens using username instead of UserDetails object
            String accessToken = jwtTokenProvider.generateAccessToken(
                    user.getEmail(),  // Use email/username as String
                    user.getId(),
                    user.getRole().name()
            );
            String refreshToken = jwtTokenProvider.generateRefreshToken(
                    user.getEmail(),  // Use email/username as String
                    user.getId()
            );

            // Create session
            var session = sessionService.createSession(user, refreshToken, request.getDeviceId(), httpRequest);

            log.info("User logged in successfully: {}", user.getEmail());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .requiresTotpVerification(user.isTotpEnabled())
                    .requiresDeviceVerification(false)
                    .sessionId(session.getId())
                    .build();

        } catch (BadCredentialsException e) {
            user.incrementFailedAttempts();

            if (user.getFailedLoginAttempts() >= maxLoginAttempts) {
                user.lock(lockDuration);
                loginHistoryService.recordLoginAttempt(user, httpRequest, false, ErrorCodes.AUTH_002);
            } else {
                loginHistoryService.recordLoginAttempt(user, httpRequest, false, ErrorCodes.AUTH_001);
            }

            userRepository.save(user);

            throw new UnauthorizedException(ErrorCodes.AUTH_001);
        } catch (Exception e) {
            user.incrementFailedAttempts();

            if (user.getFailedLoginAttempts() >= maxLoginAttempts) {
                user.lock(lockDuration);
                loginHistoryService.recordLoginAttempt(user, httpRequest, false, ErrorCodes.AUTH_002);
            } else {
                loginHistoryService.recordLoginAttempt(user, httpRequest, false, ErrorCodes.AUTH_001);
            }

            userRepository.save(user);

            log.error("Login authentication failed: {}", e.getMessage());
            throw new UnauthorizedException(ErrorCodes.AUTH_001);
        }
    }

    @Transactional
    public void logout(Long userId, Long sessionId) {
        sessionService.logoutSession(sessionId, userId);
        log.info("User logged out - User ID: {}", userId);
    }

    @Transactional
    public void logoutAll(Long userId) {
        sessionService.logoutAllSessions(userId);
        log.info("All sessions logged out for user ID: {}", userId);
    }

    // ==================== PASSWORD MANAGEMENT ====================

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        // Send password reset OTP
        otpService.generateAndSendOtp(user.getEmail(), OtpType.PASSWORD_RESET);

        log.info("Password reset OTP sent for user: {}", user.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        // Verify OTP
        otpService.verifyOtp(request.getEmail(), request.getOtp(), OtpType.PASSWORD_RESET);

        // Validate new password
        PasswordValidator.ValidationResult passwordValidation = passwordValidator.validate(request.getNewPassword());
        if (!passwordValidation.isValid()) {
            throw new InvalidRequestException("Password validation failed: " + passwordValidation.getErrorMessage());
        }

        // Update password
        String hashedPassword = encryptionUtil.hashPassword(request.getNewPassword());
        user.setPassword(hashedPassword);
        user.setPasswordChangedAt(LocalDateTime.now());
        user.resetFailedAttempts();
        userRepository.save(user);

        // Logout all sessions
        sessionService.logoutAllSessions(user.getId());

        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Verify current password
        if (!encryptionUtil.verifyPassword(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCodes.AUTH_001, "AUTH_001");
        }

        // Validate new password matches confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("New password and confirm password do not match");
        }

        // Validate new password strength
        PasswordValidator.ValidationResult passwordValidation = passwordValidator.validate(request.getNewPassword());
        if (!passwordValidation.isValid()) {
            throw new InvalidRequestException("Password validation failed: " + passwordValidation.getErrorMessage());
        }

        // Update password
        String hashedPassword = encryptionUtil.hashPassword(request.getNewPassword());
        user.setPassword(hashedPassword);
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", user.getEmail());
    }

    // ==================== ADMIN LOGIN ====================

    @Transactional
    public LoginResponse adminLogin(LoginRequest request, HttpServletRequest httpRequest) {
        // Find admin user
        User admin = userRepository.findByEmail(request.getIdentifier())
                .orElseThrow(() -> new UnauthorizedException(ErrorCodes.AUTH_001));

        if (admin.getRole() != UserRole.ADMIN &&
                admin.getRole() != UserRole.SUPER_ADMIN) {
            throw new UnauthorizedException(ErrorCodes.AUTH_001);
        }

        // Check if account is locked
        if (admin.isAccountLocked()) {
            loginHistoryService.recordLoginAttempt(admin, httpRequest, false, ErrorCodes.AUTH_002);
            throw new AccountLockedException();
        }

        // Authenticate
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(admin.getEmail(), request.getPassword())
            );

            // Reset failed attempts
            admin.resetFailedAttempts();
            admin.setLastLoginAt(LocalDateTime.now());
            userRepository.save(admin);

            // Record login
            loginHistoryService.recordLoginAttempt(admin, httpRequest, true, null);

            // Generate tokens using username instead of UserDetails object
            String accessToken = jwtTokenProvider.generateAccessToken(
                    admin.getEmail(),  // Use email/username as String
                    admin.getId(),
                    admin.getRole().name()
            );
            String refreshToken = jwtTokenProvider.generateRefreshToken(
                    admin.getEmail(),  // Use email/username as String
                    admin.getId()
            );

            // Create session
            var session = sessionService.createSession(admin, refreshToken, null, httpRequest);

            log.info("Admin logged in successfully: {}", admin.getEmail());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .userId(admin.getId())
                    .email(admin.getEmail())
                    .role(admin.getRole().name())
                    .sessionId(session.getId())
                    .build();

        } catch (BadCredentialsException e) {
            admin.incrementFailedAttempts();

            if (admin.getFailedLoginAttempts() >= maxLoginAttempts) {
                admin.lock(lockDuration);
                loginHistoryService.recordLoginAttempt(admin, httpRequest, false, ErrorCodes.AUTH_002);
            } else {
                loginHistoryService.recordLoginAttempt(admin, httpRequest, false, ErrorCodes.AUTH_001);
            }

            userRepository.save(admin);

            throw new UnauthorizedException(ErrorCodes.AUTH_001);
        } catch (Exception e) {
            admin.incrementFailedAttempts();

            if (admin.getFailedLoginAttempts() >= maxLoginAttempts) {
                admin.lock(lockDuration);
                loginHistoryService.recordLoginAttempt(admin, httpRequest, false, ErrorCodes.AUTH_002);
            } else {
                loginHistoryService.recordLoginAttempt(admin, httpRequest, false, ErrorCodes.AUTH_001);
            }

            userRepository.save(admin);

            log.error("Admin login authentication failed: {}", e.getMessage());
            throw new UnauthorizedException(ErrorCodes.AUTH_001);
        }
    }
}