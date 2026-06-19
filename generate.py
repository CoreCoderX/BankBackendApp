from pathlib import Path

BASE_DIR = Path("src/main/java/com/dvein/banking_backend")

structure = {
    "auth": {
        "controller": [
            "AuthController.java",
            "MpinController.java",
            "TotpController.java",
            "BiometricController.java",
            "DeviceController.java",
            "SessionController.java",
        ],
        "dto/request": [
            "LoginRequest.java",
            "RegisterRequest.java",
            "VerifyOtpRequest.java",
            "ForgotPasswordRequest.java",
            "ResetPasswordRequest.java",
            "ChangePasswordRequest.java",
            "CreateMpinRequest.java",
            "VerifyMpinRequest.java",
            "EnableTotpRequest.java",
            "VerifyTotpRequest.java",
            "BiometricToggleRequest.java",
            "RegisterDeviceRequest.java",
        ],
        "dto/response": [
            "LoginResponse.java",
            "RegisterResponse.java",
            "TotpSetupResponse.java",
            "DeviceResponse.java",
            "SessionResponse.java",
            "LoginHistoryResponse.java",
        ],
        "model": [
            "User.java",
            "Otp.java",
            "TotpSecret.java",
            "Mpin.java",
            "Device.java",
            "Session.java",
            "LoginHistory.java",
            "Role.java",
            "Permission.java",
            "RolePermission.java",
        ],
        "repository": [
            "UserRepository.java",
            "OtpRepository.java",
            "TotpSecretRepository.java",
            "MpinRepository.java",
            "DeviceRepository.java",
            "SessionRepository.java",
            "LoginHistoryRepository.java",
            "RoleRepository.java",
            "PermissionRepository.java",
            "RolePermissionRepository.java",
        ],
        "service": [
            "AuthService.java",
            "OtpService.java",
            "TotpService.java",
            "MpinService.java",
            "BiometricService.java",
            "DeviceService.java",
            "SessionService.java",
            "LoginHistoryService.java",
        ],
    },

    "account": {
        "controller": [
            "CustomerController.java",
            "AccountController.java",
            "BeneficiaryController.java",
            "NomineeController.java",
            "KycController.java",
            "DocumentController.java",
            "VerificationController.java",
        ],
        "dto/request": [
            "UpdateProfileRequest.java",
            "CreateAccountRequest.java",
            "AddBeneficiaryRequest.java",
            "AddNomineeRequest.java",
            "UploadDocumentRequest.java",
            "KycSubmissionRequest.java",
            "ConsentRequest.java",
        ],
        "dto/response": [
            "CustomerProfileResponse.java",
            "AccountResponse.java",
            "BeneficiaryResponse.java",
            "NomineeResponse.java",
            "DocumentResponse.java",
            "KycStatusResponse.java",
            "AccountVerificationResponse.java",
        ],
        "model": [
            "Customer.java",
            "Account.java",
            "Beneficiary.java",
            "Nominee.java",
            "Document.java",
            "Kyc.java",
            "Consent.java",
        ],
        "repository": [
            "CustomerRepository.java",
            "AccountRepository.java",
            "BeneficiaryRepository.java",
            "NomineeRepository.java",
            "DocumentRepository.java",
            "KycRepository.java",
            "ConsentRepository.java",
        ],
        "service": [
            "CustomerService.java",
            "AccountService.java",
            "BeneficiaryService.java",
            "NomineeService.java",
            "DocumentService.java",
            "KycService.java",
            "VerificationService.java",
        ],
    },

    "card": {
        "controller": [
            "DebitCardController.java",
            "CreditCardController.java",
        ],
        "dto/request": [
            "GenerateDebitCardRequest.java",
            "SetCardPinRequest.java",
            "BlockCardRequest.java",
            "ApplyCreditCardRequest.java",
            "CardSecuritySettingsRequest.java",
        ],
        "dto/response": [
            "DebitCardResponse.java",
            "CreditCardResponse.java",
        ],
        "model": [
            "DebitCard.java",
            "CreditCard.java",
            "CardSecuritySettings.java",
        ],
        "repository": [
            "DebitCardRepository.java",
            "CreditCardRepository.java",
            "CardSecuritySettingsRepository.java",
        ],
        "service": [
            "DebitCardService.java",
            "CreditCardService.java",
        ],
    },

    "admin": {
        "controller": [
            "AdminAuthController.java",
            "AdminCustomerController.java",
            "AdminDashboardController.java",
            "AuditController.java",
        ],
        "dto/request": [
            "AdminLoginRequest.java",
            "ApproveKycRequest.java",
            "ApproveCreditCardRequest.java",
            "CustomerSearchRequest.java",
        ],
        "dto/response": [
            "AdminDashboardResponse.java",
            "CustomerListResponse.java",
            "AuditLogResponse.java",
        ],
        "model": [
            "AuditLog.java",
        ],
        "repository": [
            "AuditLogRepository.java",
        ],
        "service": [
            "AdminAuthService.java",
            "AdminCustomerService.java",
            "AdminDashboardService.java",
            "AuditService.java",
        ],
    },

    "common": {
        "config": [
            "SecurityConfig.java",
            "JwtConfig.java",
            "CookieConfig.java",
            "OpenApiConfig.java",
            "CorsConfig.java",
            "AsyncConfig.java",
        ],
        "security": [
            "JwtAuthenticationFilter.java",
            "JwtTokenProvider.java",
            "CookieUtil.java",
            "CustomUserDetailsService.java",
            "DeviceFingerprint.java",
            "SecurityContextHelper.java",
        ],
        "exception": [
            "GlobalExceptionHandler.java",
            "CustomException.java",
            "UnauthorizedException.java",
            "AccountLockedException.java",
            "OtpExpiredException.java",
            "InvalidOtpException.java",
            "ResourceNotFoundException.java",
            "DuplicateResourceException.java",
            "InvalidRequestException.java",
        ],
        "util": [
            "PasswordValidator.java",
            "AccountNumberGenerator.java",
            "CardNumberGenerator.java",
            "QrCodeGenerator.java",
            "EncryptionUtil.java",
            "ValidationUtil.java",
            "DateUtil.java",
            "RandomUtil.java",
        ],
        "constant": [
            "AppConstants.java",
            "ErrorCodes.java",
            "SuccessMessages.java",
        ],
        "enums": [
            "UserRole.java",
            "AccountType.java",
            "AccountStatus.java",
            "CustomerStatus.java",
            "CardStatus.java",
            "OtpType.java",
            "KycStatus.java",
            "DocumentType.java",
            "TransactionType.java",
            "AuditAction.java",
        ],
        "dto": [
            "ApiResponse.java",
            "ErrorResponse.java",
            "PageResponse.java",
        ],
        "aspect": [
            "AuditAspect.java",
            "RateLimitAspect.java",
        ],
        "annotation": [
            "Audited.java",
            "RateLimited.java",
            "RequireRole.java",
        ],
    },

    "notification": {
        "service": [
            "EmailService.java",
        ],
        "template": [
            "EmailTemplates.java",
        ],
    }
}


def create_structure(base_path, tree):
    for folder, content in tree.items():
        current = base_path / folder

        if isinstance(content, dict):
            current.mkdir(parents=True, exist_ok=True)
            create_structure(current, content)

        elif isinstance(content, list):
            current.mkdir(parents=True, exist_ok=True)

            for file_name in content:
                file_path = current / file_name
                file_path.touch(exist_ok=True)


create_structure(BASE_DIR, structure)

print(f"Project structure created successfully at: {BASE_DIR.resolve()}")