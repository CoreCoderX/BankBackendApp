package com.dvein.banking_backend.auth.service;

import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.repository.CustomerRepository;
import com.dvein.banking_backend.auth.dto.request.*;
import com.dvein.banking_backend.auth.dto.response.LoginResponse;
import com.dvein.banking_backend.auth.dto.response.RegisterResponse;
import com.dvein.banking_backend.auth.model.PreAuthenticationSession;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.PreAuthenticationSessionRepository;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.constant.ErrorCodes;
import com.dvein.banking_backend.common.constant.SuccessMessages;
import com.dvein.banking_backend.common.enums.AuthenticationState;
import com.dvein.banking_backend.common.enums.CustomerStatus;
import com.dvein.banking_backend.common.enums.OtpType;
import com.dvein.banking_backend.common.enums.UserRole;
import com.dvein.banking_backend.common.exception.*;
import com.dvein.banking_backend.common.security.DeviceFingerprint;
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
import com.dvein.banking_backend.auth.service.TokenBlacklistService;
import com.dvein.banking_backend.common.config.JwtConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PreAuthenticationSessionRepository preAuthSessionRepository;
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
    private final DeviceFingerprint deviceFingerprint;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtConfig jwtConfig;

    @Value("${security.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${security.account-lock-duration:900000}")
    private long lockDuration;

    @Value("${security.pre-auth-token-expiry:300000}")
    private long preAuthTokenExpiry;

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
                    "Password validation failed: " + passwordValidation.getErrorMessage());
        }

        // Hash password
        String hashedPassword = encryptionUtil.hashPassword(request.getPassword());

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
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(LocalDate.parse(request.getDateOfBirth()))
                .status(CustomerStatus.ACTIVE)
                .build();

        customerRepository.save(customer);

        // Send verification OTP
        otpService.generateAndSendOtp(user.getEmail(), OtpType.EMAIL_VERIFICATION);

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

    // ==================== MULTI-STEP LOGIN ====================

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // STEP 1: Find and validate user
        User user = userRepository.findByEmailOrPhone(request.getIdentifier())
                .orElseThrow(() -> {
                    loginHistoryService.recordLoginAttempt(null, httpRequest, false, ErrorCodes.AUTH_001);
                    return new UnauthorizedException(ErrorCodes.AUTH_001);
                });

        // STEP 2: Check account lock status
        if (user.isAccountLocked()) {
            loginHistoryService.recordLoginAttempt(user, httpRequest, false, ErrorCodes.AUTH_002);
            throw new AccountLockedException();
        }

        // STEP 3: Check if user is active
        if (!user.isActive()) {
            loginHistoryService.recordLoginAttempt(user, httpRequest, false, ErrorCodes.AUTH_007);
            throw new CustomException(ErrorCodes.AUTH_007, "AUTH_007");
        }

        // STEP 4: Check if email is verified
        if (!user.isEmailVerified()) {
            loginHistoryService.recordLoginAttempt(user, httpRequest, false, ErrorCodes.AUTH_008);
            throw new CustomException(ErrorCodes.AUTH_008, "AUTH_008");
        }

        // STEP 5: Check customer status (if customer)
        if (user.getRole() == UserRole.CUSTOMER) {
            Customer customer = customerRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "userId", user.getId()));

            if (customer.getStatus() != CustomerStatus.ACTIVE) {
                loginHistoryService.recordLoginAttempt(user, httpRequest, false, "ACCOUNT_" + customer.getStatus());
                throw new InvalidRequestException("Account is " + customer.getStatus());
            }
        }

        // STEP 6: Authenticate credentials
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            handleFailedLogin(user, httpRequest);
            throw new UnauthorizedException(ErrorCodes.AUTH_001);
        } catch (Exception e) {
            handleFailedLogin(user, httpRequest);
            log.error("Login authentication failed: {}", e.getMessage());
            throw new UnauthorizedException(ErrorCodes.AUTH_001);
        }

        // STEP 7: Reset failed attempts on successful credential validation
        user.resetFailedAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // STEP 8: Deactivate any existing pre-auth sessions
        preAuthSessionRepository.deactivateAllByUser(user);

        // STEP 9: Determine authentication requirements
        boolean requiresDeviceVerification = false;
        boolean requiresTotpVerification = user.isTotpEnabled();

        // Check if device is new/untrusted
        if (request.getDeviceId() != null) {
            if (!deviceService.deviceExists(request.getDeviceId())) {
                requiresDeviceVerification = true;
            } else if (!deviceService.isDeviceTrusted(user.getId(), request.getDeviceId())) {
                requiresDeviceVerification = true;
            } else {
                // Update existing trusted device activity
                deviceService.updateDeviceActivity(request.getDeviceId(), httpRequest);
            }
        }

        // STEP 10: If MFA is required, create pre-auth session
        if (requiresDeviceVerification || requiresTotpVerification) {
            return createPreAuthenticationSession(
                    user,
                    request,
                    httpRequest,
                    requiresDeviceVerification,
                    requiresTotpVerification
            );
        }

        // STEP 11: If no MFA required, complete authentication immediately
        return completeAuthentication(user, request, httpRequest);
    }

    private LoginResponse createPreAuthenticationSession(
            User user,
            LoginRequest request,
            HttpServletRequest httpRequest,
            boolean requiresDeviceVerification,
            boolean requiresTotpVerification) {

        // Generate pre-authentication token
        String preAuthToken = jwtTokenProvider.generatePreAuthToken(
                user.getEmail(),
                user.getId(),
                user.getRole().name()
        );

        // Determine initial authentication state
        AuthenticationState initialState;
        if (requiresDeviceVerification) {
            initialState = AuthenticationState.REQUIRES_DEVICE_VERIFICATION;
            // Send device verification OTP
            otpService.generateAndSendOtp(user.getEmail(), OtpType.DEVICE_VERIFICATION);
        } else if (requiresTotpVerification) {
            initialState = AuthenticationState.REQUIRES_TOTP;
        } else {
            initialState = AuthenticationState.CREDENTIALS_VALIDATED;
        }

        // Create pre-auth session
        PreAuthenticationSession preAuthSession = PreAuthenticationSession.builder()
                .user(user)
                .preAuthToken(preAuthToken)
                .authenticationState(initialState)
                .deviceId(request.getDeviceId())
                .ipAddress(deviceFingerprint.getIpAddress(httpRequest))
                .userAgent(deviceFingerprint.getUserAgent(httpRequest))
                .deviceVerified(!requiresDeviceVerification)
                .totpVerified(!requiresTotpVerification)
                .expiresAt(LocalDateTime.now().plusSeconds(preAuthTokenExpiry / 1000))
                .build();

        preAuthSession = preAuthSessionRepository.save(preAuthSession);

        // Record partial login
        loginHistoryService.recordLoginAttempt(user, httpRequest, true, "MFA_REQUIRED");

        log.info("Pre-authentication session created for user: {} - State: {}",
                user.getEmail(), initialState);

        String message;
        if (requiresDeviceVerification) {
            message = "Device verification required. Please check your email for verification code.";
        } else {
            message = "TOTP verification required. Please enter your authenticator code.";
        }

        return LoginResponse.builder()
                .authenticationState(initialState)
                .preAuthToken(preAuthToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .requiresDeviceVerification(requiresDeviceVerification)
                .requiresTotpVerification(requiresTotpVerification)
                .preAuthSessionId(preAuthSession.getId())
                .message(message)
                .expiresIn(preAuthTokenExpiry / 1000)
                .build();
    }

    @Transactional
    public LoginResponse verifyDeviceForLogin(VerifyDeviceRequest request, HttpServletRequest httpRequest) {
        // Validate pre-auth token
        PreAuthenticationSession preAuthSession = preAuthSessionRepository
                .findByPreAuthToken(request.getPreAuthToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired pre-authentication token"));

        if (!preAuthSession.isActive() || preAuthSession.isExpired()) {
            throw new UnauthorizedException("Pre-authentication session expired");
        }

        if (preAuthSession.getAuthenticationState() != AuthenticationState.REQUIRES_DEVICE_VERIFICATION) {
            throw new InvalidRequestException("Device verification not required in current state");
        }

        User user = preAuthSession.getUser();

        // Verify device OTP
        otpService.verifyOtp(user.getEmail(), request.getVerificationCode(), OtpType.DEVICE_VERIFICATION);

        // Register or update device
        if (preAuthSession.getDeviceId() != null) {
            if (!deviceService.deviceExists(preAuthSession.getDeviceId())) {
                RegisterDeviceRequest deviceRequest = RegisterDeviceRequest.builder()
                        .deviceId(preAuthSession.getDeviceId())
                        .deviceName(deviceFingerprint.getUserAgent(httpRequest))
                        .trusted(request.isTrustDevice())
                        .build();
                deviceService.registerDevice(user.getId(), deviceRequest, httpRequest);
            } else if (request.isTrustDevice()) {
                deviceService.updateDeviceTrustStatus(preAuthSession.getDeviceId(), true);
            }
        }

        // Mark device as verified
        preAuthSession.markDeviceVerified();
        preAuthSessionRepository.save(preAuthSession);

        log.info("Device verified for user: {}", user.getEmail());

        // Check if TOTP is still required
        if (user.isTotpEnabled() && !preAuthSession.isTotpVerified()) {
            preAuthSession.setAuthenticationState(AuthenticationState.REQUIRES_TOTP);
            preAuthSessionRepository.save(preAuthSession);

            return LoginResponse.builder()
                    .authenticationState(AuthenticationState.REQUIRES_TOTP)
                    .preAuthToken(request.getPreAuthToken())
                    .userId(user.getId())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .requiresTotpVerification(true)
                    .preAuthSessionId(preAuthSession.getId())
                    .message("Device verified. TOTP verification required.")
                    .expiresIn(preAuthTokenExpiry / 1000)
                    .build();
        }

        // Complete authentication if no more steps required
        return finalizeAuthentication(preAuthSession, httpRequest);
    }

    @Transactional
    public LoginResponse verifyTotpForLogin(VerifyTotpRequest request, HttpServletRequest httpRequest) {
        // Validate pre-auth token
        PreAuthenticationSession preAuthSession = preAuthSessionRepository
                .findByPreAuthToken(request.getPreAuthToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired pre-authentication token"));

        if (!preAuthSession.isActive() || preAuthSession.isExpired()) {
            throw new UnauthorizedException("Pre-authentication session expired");
        }

        if (preAuthSession.getAuthenticationState() != AuthenticationState.REQUIRES_TOTP) {
            throw new InvalidRequestException("TOTP verification not required in current state");
        }

        User user = preAuthSession.getUser();

        // Verify TOTP code
        boolean totpValid = totpService.verifyTotp(user.getId(), request.getCode());
        if (!totpValid) {
            loginHistoryService.recordLoginAttempt(user, httpRequest, false, "INVALID_TOTP");
            throw new UnauthorizedException("Invalid TOTP code");
        }

        // Mark TOTP as verified
        preAuthSession.markTotpVerified();
        preAuthSessionRepository.save(preAuthSession);

        log.info("TOTP verified for user: {}", user.getEmail());

        // Complete authentication
        return finalizeAuthentication(preAuthSession, httpRequest);
    }

    private LoginResponse finalizeAuthentication(
            PreAuthenticationSession preAuthSession,
            HttpServletRequest httpRequest) {

        User user = preAuthSession.getUser();

        // Generate final JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getEmail(),
                user.getId(),
                user.getRole().name()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getEmail(),
                user.getId()
        );

        // Create permanent session
        var session = sessionService.createSession(
                user,
                refreshToken,
                preAuthSession.getDeviceId(),
                httpRequest
        );

        // Complete pre-auth session
        preAuthSession.complete();
        preAuthSessionRepository.save(preAuthSession);

        // Record successful login
        loginHistoryService.recordLoginAttempt(user, httpRequest, true, null);

        log.info("User fully authenticated: {}", user.getEmail());

        return LoginResponse.builder()
                .authenticationState(AuthenticationState.FULLY_AUTHENTICATED)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .sessionId(session.getId())
                .message("Authentication successful")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirySeconds())
                .build();
    }

    private LoginResponse completeAuthentication(
            User user,
            LoginRequest request,
            HttpServletRequest httpRequest) {

        // Register device if provided
        if (request.getDeviceId() != null && request.getDeviceName() != null) {
            if (!deviceService.deviceExists(request.getDeviceId())) {
                RegisterDeviceRequest deviceRequest = RegisterDeviceRequest.builder()
                        .deviceId(request.getDeviceId())
                        .deviceName(request.getDeviceName())
                        .trusted(true)
                        .build();
                deviceService.registerDevice(user.getId(), deviceRequest, httpRequest);
            }
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getEmail(),
                user.getId(),
                user.getRole().name()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getEmail(),
                user.getId()
        );

        // Create session
        var session = sessionService.createSession(user, refreshToken, request.getDeviceId(), httpRequest);

        // Record successful login
        loginHistoryService.recordLoginAttempt(user, httpRequest, true, null);

        log.info("User logged in successfully: {}", user.getEmail());

        return LoginResponse.builder()
                .authenticationState(AuthenticationState.FULLY_AUTHENTICATED)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .sessionId(session.getId())
                .message("Login successful")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirySeconds())
                .build();
    }

    private void handleFailedLogin(User user, HttpServletRequest httpRequest) {
        user.incrementFailedAttempts();

        if (user.getFailedLoginAttempts() >= maxLoginAttempts) {
            user.lock(lockDuration);
            loginHistoryService.recordLoginAttempt(user, httpRequest, false, ErrorCodes.AUTH_002);
            log.warn("Account locked due to multiple failed login attempts: {}", user.getEmail());
        } else {
            loginHistoryService.recordLoginAttempt(user, httpRequest, false, ErrorCodes.AUTH_001);
        }

        userRepository.save(user);
    }

    @Transactional
    public void logout(Long userId, Long sessionId) {
        sessionService.logoutSession(sessionId, userId);

        // Deactivate any active pre-auth sessions
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        preAuthSessionRepository.deactivateAllByUser(user);

        log.info("User logged out - User ID: {}", userId);
    }

    @Transactional
    public void logoutAll(Long userId) {
        sessionService.logoutAllSessions(userId);

        // Deactivate any active pre-auth sessions
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        preAuthSessionRepository.deactivateAllByUser(user);

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
        PasswordValidator.ValidationResult passwordValidation =
                passwordValidator.validate(request.getNewPassword());
        if (!passwordValidation.isValid()) {
            throw new InvalidRequestException(
                    "Password validation failed: " + passwordValidation.getErrorMessage());
        }

        // Update password
        String hashedPassword = encryptionUtil.hashPassword(request.getNewPassword());
        user.setPassword(hashedPassword);
        user.setPasswordChangedAt(LocalDateTime.now());
        user.resetFailedAttempts();
        userRepository.save(user);

        // Logout all sessions
        sessionService.logoutAllSessions(user.getId());
        preAuthSessionRepository.deactivateAllByUser(user);

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
        PasswordValidator.ValidationResult passwordValidation =
                passwordValidator.validate(request.getNewPassword());
        if (!passwordValidation.isValid()) {
            throw new InvalidRequestException(
                    "Password validation failed: " + passwordValidation.getErrorMessage());
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

        if (admin.getRole() != UserRole.ADMIN && admin.getRole() != UserRole.SUPER_ADMIN) {
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
        } catch (BadCredentialsException e) {
            handleFailedLogin(admin, httpRequest);
            throw new UnauthorizedException(ErrorCodes.AUTH_001);
        } catch (Exception e) {
            handleFailedLogin(admin, httpRequest);
            log.error("Admin login authentication failed: {}", e.getMessage());
            throw new UnauthorizedException(ErrorCodes.AUTH_001);
        }

        // Reset failed attempts
        admin.resetFailedAttempts();
        admin.setLastLoginAt(LocalDateTime.now());
        userRepository.save(admin);

        // Record login
        loginHistoryService.recordLoginAttempt(admin, httpRequest, true, null);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                admin.getEmail(),
                admin.getId(),
                admin.getRole().name()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                admin.getEmail(),
                admin.getId()
        );

        // Create session
        var session = sessionService.createSession(admin, refreshToken, null, httpRequest);

        log.info("Admin logged in successfully: {}", admin.getEmail());

        return LoginResponse.builder()
                .authenticationState(AuthenticationState.FULLY_AUTHENTICATED)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(admin.getId())
                .email(admin.getEmail())
                .role(admin.getRole().name())
                .sessionId(session.getId())
                .message("Admin login successful")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirySeconds())
                .build();
    }

    @Transactional
    public LoginResponse refreshAccessToken(String refreshToken, HttpServletRequest request) {
        // Validate refresh token
        if (!sessionService.isSessionValid(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        // Check if blacklisted
        if (tokenBlacklistService.isTokenBlacklisted(refreshToken)) {
            throw new UnauthorizedException("Token has been revoked");
        }

        // Extract user from token
        String userEmail = jwtTokenProvider.extractUsername(refreshToken);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getEmail(),
                user.getId(),
                user.getRole().name()
        );

        // Update session activity
        sessionService.updateSessionActivity(refreshToken);

        log.info("Access token refreshed for user: {}", user.getEmail());

        return LoginResponse.builder()
                .authenticationState(AuthenticationState.FULLY_AUTHENTICATED)
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Return same refresh token
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(jwtConfig.getAccessTokenExpiry() / 1000)
                .build();
    }
}