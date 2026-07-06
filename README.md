# Banking Backend Project

## Module - 1 (Sivaprakash)

### Module Breakdown

### 1. Auth Module

```text
auth/
├── controller (6)
├── dto/request (14)
├── dto/response (6)
├── model (12)
├── repository (12)
└── service (10)
```

#### Features

* Registration & Login
* PRE_AUTH Authentication Flow
* Device Verification
* OTP Verification
* TOTP (MFA)
* Password Reset
* MPIN Management
* Device Management
* Session Management
* Login History
* Token Blacklisting
* Authentication Cleanup Jobs

---

### 2. Account Module

```text
account/
├── controller (7)
├── dto/request (7)
├── dto/response (7)
├── model (7)
├── repository (7)
└── service (7)
```

#### Features

* Customer Profile Management
* Bank Account Management
* Beneficiary Management
* Nominee Management
* Document Management
* KYC Processing
* Account Verification APIs

---

### 3. Card Module

```text
card/
├── controller (2)
├── dto/request (5)
├── dto/response (2)
├── model (3)
├── repository (3)
└── service (2)
```

#### Features

* Debit Card Management
* Credit Card Management
* Card PIN Management
* Card Security Settings

---

### 4. Admin Module

```text
admin/
├── controller (5)
├── dto/request (8)
├── dto/response (6)
├── model (1)
├── repository (1)
└── service (4)
```

#### Features

* Admin Authentication
* Customer Management
* KYC Approval & Rejection
* Credit Card Approval & Rejection
* Dashboard & Analytics
* Audit Logs

---

### 5. Common Module

```text
common/
├── annotation
├── aspect
├── config
├── constant
├── dto
├── enums
├── exception
├── security
└── util
```

#### Features

* JWT Security
* Role-Based Authorization
* Device Fingerprinting
* Global Exception Handling
* Audit Logging Support
* Rate Limiting
* Utility Classes
* Application Configuration
* Common DTOs & Constants
* Authentication State Management

---

### Database Migration

```text
resources/db/migration/
├── V1__initial_schema.sql
├── V2__add_authentication_enhancements.sql
└── V3__fix_schema_constraints.sql
```

#### Features

* FlywayDB Integration
* Versioned Database Migrations
* Authentication Schema Enhancements
* Database Constraint Fixes

``` 
```

---

## Final Folder Structure

```text
PS $tree src /F > structure.txt    
Folder PATH listing for volume Windows-SSD
Volume serial number is 0000021E C20D:95B5
C:\USERS\SIVAP\DOWNLOADS\BANK-BACKEND\SRC
+---main
ª   +---java
ª   ª   +---com
ª   ª       +---dvein
ª   ª           +---banking_backend
ª   ª               ª   BankingBackendApplication.java
ª   ª               ª   
ª   ª               +---account
ª   ª               ª   +---controller
ª   ª               ª   ª       AccountController.java
ª   ª               ª   ª       BeneficiaryController.java
ª   ª               ª   ª       CustomerController.java
ª   ª               ª   ª       DocumentController.java
ª   ª               ª   ª       KycController.java
ª   ª               ª   ª       NomineeController.java
ª   ª               ª   ª       VerificationController.java
ª   ª               ª   ª       
ª   ª               ª   +---dto
ª   ª               ª   ª   +---request
ª   ª               ª   ª   ª       AddBeneficiaryRequest.java
ª   ª               ª   ª   ª       AddNomineeRequest.java
ª   ª               ª   ª   ª       ConsentRequest.java
ª   ª               ª   ª   ª       CreateAccountRequest.java
ª   ª               ª   ª   ª       KycSubmissionRequest.java
ª   ª               ª   ª   ª       UpdateProfileRequest.java
ª   ª               ª   ª   ª       UploadDocumentRequest.java
ª   ª               ª   ª   ª       
ª   ª               ª   ª   +---response
ª   ª               ª   ª           AccountResponse.java
ª   ª               ª   ª           AccountVerificationResponse.java
ª   ª               ª   ª           BeneficiaryResponse.java
ª   ª               ª   ª           CustomerProfileResponse.java
ª   ª               ª   ª           DocumentResponse.java
ª   ª               ª   ª           KycStatusResponse.java
ª   ª               ª   ª           NomineeResponse.java
ª   ª               ª   ª           
ª   ª               ª   +---model
ª   ª               ª   ª       Account.java
ª   ª               ª   ª       Beneficiary.java
ª   ª               ª   ª       Consent.java
ª   ª               ª   ª       Customer.java
ª   ª               ª   ª       Document.java
ª   ª               ª   ª       Kyc.java
ª   ª               ª   ª       Nominee.java
ª   ª               ª   ª       
ª   ª               ª   +---repository
ª   ª               ª   ª       AccountRepository.java
ª   ª               ª   ª       BeneficiaryRepository.java
ª   ª               ª   ª       ConsentRepository.java
ª   ª               ª   ª       CustomerRepository.java
ª   ª               ª   ª       DocumentRepository.java
ª   ª               ª   ª       KycRepository.java
ª   ª               ª   ª       NomineeRepository.java
ª   ª               ª   ª       
ª   ª               ª   +---service
ª   ª               ª           AccountService.java
ª   ª               ª           BeneficiaryService.java
ª   ª               ª           CustomerService.java
ª   ª               ª           DocumentService.java
ª   ª               ª           KycService.java
ª   ª               ª           NomineeService.java
ª   ª               ª           VerificationService.java
ª   ª               ª           
ª   ª               +---admin
ª   ª               ª   +---controller
ª   ª               ª   ª       AdminAuthController.java
ª   ª               ª   ª       AdminCardController.java
ª   ª               ª   ª       AdminCustomerController.java
ª   ª               ª   ª       AdminDashboardController.java
ª   ª               ª   ª       AuditController.java
ª   ª               ª   ª       
ª   ª               ª   +---dto
ª   ª               ª   ª   +---request
ª   ª               ª   ª   ª       AdminLoginRequest.java
ª   ª               ª   ª   ª       ApproveCreditCardRequest.java
ª   ª               ª   ª   ª       ApproveKycRequest.java
ª   ª               ª   ª   ª       CreateAdminRequest.java
ª   ª               ª   ª   ª       CustomerSearchRequest.java
ª   ª               ª   ª   ª       RejectCreditCardRequest.java
ª   ª               ª   ª   ª       RejectKycRequest.java
ª   ª               ª   ª   ª       UpdateCustomerStatusRequest.java
ª   ª               ª   ª   ª       
ª   ª               ª   ª   +---response
ª   ª               ª   ª           AdminDashboardResponse.java
ª   ª               ª   ª           AdminProfileResponse.java
ª   ª               ª   ª           AuditLogResponse.java
ª   ª               ª   ª           CustomerListResponse.java
ª   ª               ª   ª           PendingCreditCardResponse.java
ª   ª               ª   ª           PendingKycResponse.java
ª   ª               ª   ª           
ª   ª               ª   +---model
ª   ª               ª   ª       AuditLog.java
ª   ª               ª   ª       
ª   ª               ª   +---repository
ª   ª               ª   ª       AuditLogRepository.java
ª   ª               ª   ª       
ª   ª               ª   +---service
ª   ª               ª           AdminAuthService.java
ª   ª               ª           AdminCustomerService.java
ª   ª               ª           AdminDashboardService.java
ª   ª               ª           AuditService.java
ª   ª               ª           
ª   ª               +---auth
ª   ª               ª   +---controller
ª   ª               ª   ª       AuthController.java
ª   ª               ª   ª       BiometricController.java
ª   ª               ª   ª       DeviceController.java
ª   ª               ª   ª       MpinController.java
ª   ª               ª   ª       SessionController.java
ª   ª               ª   ª       TotpController.java
ª   ª               ª   ª       
ª   ª               ª   +---dto
ª   ª               ª   ª   +---request
ª   ª               ª   ª   ª       BiometricToggleRequest.java
ª   ª               ª   ª   ª       ChangeMpinRequest.java
ª   ª               ª   ª   ª       ChangePasswordRequest.java
ª   ª               ª   ª   ª       CreateMpinRequest.java
ª   ª               ª   ª   ª       EnableTotpRequest.java
ª   ª               ª   ª   ª       ForgotPasswordRequest.java
ª   ª               ª   ª   ª       LoginRequest.java
ª   ª               ª   ª   ª       RefreshTokenRequest.java
ª   ª               ª   ª   ª       RegisterDeviceRequest.java
ª   ª               ª   ª   ª       RegisterRequest.java
ª   ª               ª   ª   ª       ResendOtpRequest.java
ª   ª               ª   ª   ª       ResetPasswordRequest.java
ª   ª               ª   ª   ª       VerifyDeviceRequest.java
ª   ª               ª   ª   ª       VerifyMpinRequest.java
ª   ª               ª   ª   ª       VerifyOtpRequest.java
ª   ª               ª   ª   ª       VerifyTotpRequest.java
ª   ª               ª   ª   ª       
ª   ª               ª   ª   +---response
ª   ª               ª   ª           DeviceResponse.java
ª   ª               ª   ª           LoginHistoryResponse.java
ª   ª               ª   ª           LoginResponse.java
ª   ª               ª   ª           RegisterResponse.java
ª   ª               ª   ª           SessionResponse.java
ª   ª               ª   ª           TotpSetupResponse.java
ª   ª               ª   ª           
ª   ª               ª   +---model
ª   ª               ª   ª       Device.java
ª   ª               ª   ª       LoginHistory.java
ª   ª               ª   ª       Mpin.java
ª   ª               ª   ª       Otp.java
ª   ª               ª   ª       Permission.java
ª   ª               ª   ª       PreAuthenticationSession.java
ª   ª               ª   ª       Role.java
ª   ª               ª   ª       RolePermission.java
ª   ª               ª   ª       Session.java
ª   ª               ª   ª       TokenBlacklist.java
ª   ª               ª   ª       TotpSecret.java
ª   ª               ª   ª       User.java
ª   ª               ª   ª       
ª   ª               ª   +---repository
ª   ª               ª   ª       DeviceRepository.java
ª   ª               ª   ª       LoginHistoryRepository.java
ª   ª               ª   ª       MpinRepository.java
ª   ª               ª   ª       OtpRepository.java
ª   ª               ª   ª       PermissionRepository.java
ª   ª               ª   ª       PreAuthenticationSessionRepository.java
ª   ª               ª   ª       RolePermissionRepository.java
ª   ª               ª   ª       RoleRepository.java
ª   ª               ª   ª       SessionRepository.java
ª   ª               ª   ª       TokenBlacklistRepository.java
ª   ª               ª   ª       TotpSecretRepository.java
ª   ª               ª   ª       UserRepository.java
ª   ª               ª   ª       
ª   ª               ª   +---service
ª   ª               ª           AuthenticationCleanupService.java
ª   ª               ª           AuthService.java
ª   ª               ª           BiometricService.java
ª   ª               ª           DeviceService.java
ª   ª               ª           LoginHistoryService.java
ª   ª               ª           MpinService.java
ª   ª               ª           OtpService.java
ª   ª               ª           SessionService.java
ª   ª               ª           TokenBlacklistService.java
ª   ª               ª           TotpService.java
ª   ª               ª           
ª   ª               +---card
ª   ª               ª   +---controller
ª   ª               ª   ª       CreditCardController.java
ª   ª               ª   ª       DebitCardController.java
ª   ª               ª   ª       
ª   ª               ª   +---dto
ª   ª               ª   ª   +---request
ª   ª               ª   ª   ª       ApplyCreditCardRequest.java
ª   ª               ª   ª   ª       BlockCardRequest.java
ª   ª               ª   ª   ª       CardSecuritySettingsRequest.java
ª   ª               ª   ª   ª       GenerateDebitCardRequest.java
ª   ª               ª   ª   ª       SetCardPinRequest.java
ª   ª               ª   ª   ª       
ª   ª               ª   ª   +---response
ª   ª               ª   ª           CreditCardResponse.java
ª   ª               ª   ª           DebitCardResponse.java
ª   ª               ª   ª           
ª   ª               ª   +---model
ª   ª               ª   ª       CardSecuritySettings.java
ª   ª               ª   ª       CreditCard.java
ª   ª               ª   ª       DebitCard.java
ª   ª               ª   ª       
ª   ª               ª   +---repository
ª   ª               ª   ª       CardSecuritySettingsRepository.java
ª   ª               ª   ª       CreditCardRepository.java
ª   ª               ª   ª       DebitCardRepository.java
ª   ª               ª   ª       
ª   ª               ª   +---service
ª   ª               ª           CreditCardService.java
ª   ª               ª           DebitCardService.java
ª   ª               ª           
ª   ª               +---common
ª   ª               ª   +---annotation
ª   ª               ª   ª       Audited.java
ª   ª               ª   ª       RateLimited.java
ª   ª               ª   ª       RequireRole.java
ª   ª               ª   ª       
ª   ª               ª   +---aspect
ª   ª               ª   ª       AuditAspect.java
ª   ª               ª   ª       RateLimitAspect.java
ª   ª               ª   ª       
ª   ª               ª   +---config
ª   ª               ª   ª       AdminSeeder.java
ª   ª               ª   ª       AsyncConfig.java
ª   ª               ª   ª       CookieConfig.java
ª   ª               ª   ª       CorsConfig.java
ª   ª               ª   ª       JwtConfig.java
ª   ª               ª   ª       OpenApiConfig.java
ª   ª               ª   ª       SchedulingConfig.java
ª   ª               ª   ª       SecurityConfig.java
ª   ª               ª   ª       
ª   ª               ª   +---constant
ª   ª               ª   ª       AppConstants.java
ª   ª               ª   ª       ErrorCodes.java
ª   ª               ª   ª       SuccessMessages.java
ª   ª               ª   ª       TransactionConstants.java
ª   ª               ª   ª       TransactionMessages.java
ª   ª               ª   ª       
ª   ª               ª   +---dto
ª   ª               ª   ª       ApiResponse.java
ª   ª               ª   ª       ErrorResponse.java
ª   ª               ª   ª       PageResponse.java
ª   ª               ª   ª       
ª   ª               ª   +---enums
ª   ª               ª   ª       AccountStatus.java
ª   ª               ª   ª       AccountType.java
ª   ª               ª   ª       AuditAction.java
ª   ª               ª   ª       AuthenticationState.java
ª   ª               ª   ª       CardStatus.java
ª   ª               ª   ª       CustomerStatus.java
ª   ª               ª   ª       DocumentType.java
ª   ª               ª   ª       KycStatus.java
ª   ª               ª   ª       OtpType.java
ª   ª               ª   ª       TransactionType.java
ª   ª               ª   ª       UserRole.java
ª   ª               ª   ª       
ª   ª               ª   +---exception
ª   ª               ª   ª       AccountLockedException.java
ª   ª               ª   ª       CustomException.java
ª   ª               ª   ª       DuplicateResourceException.java
ª   ª               ª   ª       GlobalExceptionHandler.java
ª   ª               ª   ª       InvalidOtpException.java
ª   ª               ª   ª       InvalidRequestException.java
ª   ª               ª   ª       OtpExpiredException.java
ª   ª               ª   ª       ResourceNotFoundException.java
ª   ª               ª   ª       UnauthorizedException.java
ª   ª               ª   ª       
ª   ª               ª   +---security
ª   ª               ª   ª       CookieUtil.java
ª   ª               ª   ª       CustomUserDetailsService.java
ª   ª               ª   ª       DeviceFingerprint.java
ª   ª               ª   ª       JwtAuthenticationFilter.java
ª   ª               ª   ª       JwtTokenProvider.java
ª   ª               ª   ª       SecurityContextHelper.java
ª   ª               ª   ª       
ª   ª               ª   +---util
ª   ª               ª           AccountNumberGenerator.java
ª   ª               ª           CardNumberGenerator.java
ª   ª               ª           DateUtil.java
ª   ª               ª           EncryptionUtil.java
ª   ª               ª           PasswordValidator.java
ª   ª               ª           QrCodeGenerator.java
ª   ª               ª           RandomUtil.java
ª   ª               ª           ValidationUtil.java
ª   ª               ª           
ª   ª               +---notification
ª   ª               ª   +---service
ª   ª               ª   ª       EmailService.java
ª   ª               ª   ª       
ª   ª               ª   +---template
ª   ª               ª           EmailTemplates.java
ª   ª               ª           TransactionEmailTemplates.java
ª   ª               ª           
ª   ª               +---transaction
ª   ª                   +---controller
ª   ª                   ª       AdminMerchantController.java
ª   ª                   ª       AdminTransactionController.java
ª   ª                   ª       BillPaymentController.java
ª   ª                   ª       ExternalTransferController.java
ª   ª                   ª       InternalTransferController.java
ª   ª                   ª       MerchantPaymentController.java
ª   ª                   ª       ScheduledPaymentController.java
ª   ª                   ª       StandingInstructionController.java
ª   ª                   ª       TransactionController.java
ª   ª                   ª       TransactionLimitController.java
ª   ª                   ª       TransactionReceiptController.java
ª   ª                   ª       TransactionStatementController.java
ª   ª                   ª       UpiController.java
ª   ª                   ª       
ª   ª                   +---dto
ª   ª                   ª   +---request
ª   ª                   ª   ª       BillPaymentRequest.java
ª   ª                   ª   ª       ChangeUpiPinRequest.java
ª   ª                   ª   ª       CreateMerchantRequest.java
ª   ª                   ª   ª       CreateStandingInstructionRequest.java
ª   ª                   ª   ª       CreateUpiIdRequest.java
ª   ª                   ª   ª       CreateUpiPinRequest.java
ª   ª                   ª   ª       EstimateChargeRequest.java
ª   ª                   ª   ª       ExternalTransferRequest.java
ª   ª                   ª   ª       GenerateQrRequest.java
ª   ª                   ª   ª       ImpsTransferRequest.java
ª   ª                   ª   ª       InternalTransferRequest.java
ª   ª                   ª   ª       LinkAccountToUpiRequest.java
ª   ª                   ª   ª       MerchantPaymentRequest.java
ª   ª                   ª   ª       NeftTransferRequest.java
ª   ª                   ª   ª       RaiseDisputeRequest.java
ª   ª                   ª   ª       RefundRequest.java
ª   ª                   ª   ª       ReversalRequest.java
ª   ª                   ª   ª       RtgsTransferRequest.java
ª   ª                   ª   ª       SaveBillerRequest.java
ª   ª                   ª   ª       ScanQrRequest.java
ª   ª                   ª   ª       SchedulePaymentRequest.java
ª   ª                   ª   ª       TransactionSearchRequest.java
ª   ª                   ª   ª       UpdateFeeConfigRequest.java
ª   ª                   ª   ª       UpdateMerchantRequest.java
ª   ª                   ª   ª       UpdateTransactionLimitRequest.java
ª   ª                   ª   ª       UpdateUpiIdRequest.java
ª   ª                   ª   ª       UpiCollectMoneyRequest.java
ª   ª                   ª   ª       UpiSendMoneyRequest.java
ª   ª                   ª   ª       VerifyUpiPinRequest.java
ª   ª                   ª   ª       
ª   ª                   ª   +---response
ª   ª                   ª           BankResponse.java
ª   ª                   ª           BillerResponse.java
ª   ª                   ª           BillPaymentDetailsResponse.java
ª   ª                   ª           BillPaymentResponse.java
ª   ª                   ª           EstimatedChargeResponse.java
ª   ª                   ª           FraudAlertResponse.java
ª   ª                   ª           MerchantPaymentDetailsResponse.java
ª   ª                   ª           MerchantResponse.java
ª   ª                   ª           ScheduledPaymentResponse.java
ª   ª                   ª           SpendingAnalysisResponse.java
ª   ª                   ª           StandingInstructionResponse.java
ª   ª                   ª           TransactionDetailsResponse.java
ª   ª                   ª           TransactionDisputeDetailResponse.java
ª   ª                   ª           TransactionDisputeResponse.java
ª   ª                   ª           TransactionLimitResponse.java
ª   ª                   ª           TransactionListResponse.java
ª   ª                   ª           TransactionReceiptResponse.java
ª   ª                   ª           TransactionResponse.java
ª   ª                   ª           TransactionStatementResponse.java
ª   ª                   ª           TransactionSummaryResponse.java
ª   ª                   ª           UpiCollectRequestResponse.java
ª   ª                   ª           UpiIdResponse.java
ª   ª                   ª           UpiProfileResponse.java
ª   ª                   ª           UpiQrResponse.java
ª   ª                   ª           UpiTransactionResponse.java
ª   ª                   ª           
ª   ª                   +---enums
ª   ª                   ª       BillCategory.java
ª   ª                   ª       DisputeStatus.java
ª   ª                   ª       ExecutionStatus.java
ª   ª                   ª       FraudRiskLevel.java
ª   ª                   ª       PaymentMethod.java
ª   ª                   ª       QrType.java
ª   ª                   ª       ScheduleFrequency.java
ª   ª                   ª       TransactionMode.java
ª   ª                   ª       TransactionStatus.java
ª   ª                   ª       TransactionType.java
ª   ª                   ª       UpiStatus.java
ª   ª                   ª       
ª   ª                   +---exception
ª   ª                   ª       DuplicateTransactionException.java
ª   ª                   ª       FraudDetectedException.java
ª   ª                   ª       InsufficientBalanceException.java
ª   ª                   ª       InvalidUpiIdException.java
ª   ª                   ª       TransactionLimitExceededException.java
ª   ª                   ª       UpiPinLockedException.java
ª   ª                   ª       
ª   ª                   +---job
ª   ª                   ª       BillReminderJob.java
ª   ª                   ª       DailyLimitResetJob.java
ª   ª                   ª       ExpireCollectRequestJob.java
ª   ª                   ª       ExpireQrCodesJob.java
ª   ª                   ª       FailedTransactionRetryJob.java
ª   ª                   ª       ReconciliationJob.java
ª   ª                   ª       ScheduledPaymentExecutor.java
ª   ª                   ª       StandingInstructionExecutor.java
ª   ª                   ª       
ª   ª                   +---model
ª   ª                   ª       Bank.java
ª   ª                   ª       Biller.java
ª   ª                   ª       BillPayment.java
ª   ª                   ª       DailyReconciliation.java
ª   ª                   ª       FraudDetectionLog.java
ª   ª                   ª       Merchant.java
ª   ª                   ª       MerchantCategory.java
ª   ª                   ª       MerchantPayment.java
ª   ª                   ª       ScheduledPayment.java
ª   ª                   ª       StandingInstruction.java
ª   ª                   ª       Transaction.java
ª   ª                   ª       TransactionApproval.java
ª   ª                   ª       TransactionCategory.java
ª   ª                   ª       TransactionDispute.java
ª   ª                   ª       TransactionFeeConfig.java
ª   ª                   ª       TransactionLimit.java
ª   ª                   ª       TransactionMetadata.java
ª   ª                   ª       TransactionReceipt.java
ª   ª                   ª       UpiCollectRequest.java
ª   ª                   ª       UpiId.java
ª   ª                   ª       UpiPin.java
ª   ª                   ª       UpiProfile.java
ª   ª                   ª       UpiQrCode.java
ª   ª                   ª       UpiTransaction.java
ª   ª                   ª       
ª   ª                   +---repository
ª   ª                   ª       BankRepository.java
ª   ª                   ª       BillerRepository.java
ª   ª                   ª       BillPaymentRepository.java
ª   ª                   ª       DailyReconciliationRepository.java
ª   ª                   ª       FraudDetectionLogRepository.java
ª   ª                   ª       MerchantCategoryRepository.java
ª   ª                   ª       MerchantPaymentRepository.java
ª   ª                   ª       MerchantRepository.java
ª   ª                   ª       ScheduledPaymentRepository.java
ª   ª                   ª       StandingInstructionRepository.java
ª   ª                   ª       TransactionApprovalRepository.java
ª   ª                   ª       TransactionCategoryRepository.java
ª   ª                   ª       TransactionDisputeRepository.java
ª   ª                   ª       TransactionFeeConfigRepository.java
ª   ª                   ª       TransactionLimitRepository.java
ª   ª                   ª       TransactionMetadataRepository.java
ª   ª                   ª       TransactionReceiptRepository.java
ª   ª                   ª       TransactionRepository.java
ª   ª                   ª       UpiCollectRequestRepository.java
ª   ª                   ª       UpiIdRepository.java
ª   ª                   ª       UpiPinRepository.java
ª   ª                   ª       UpiProfileRepository.java
ª   ª                   ª       UpiQrCodeRepository.java
ª   ª                   ª       UpiTransactionRepository.java
ª   ª                   ª       
ª   ª                   +---service
ª   ª                   ª       AdminMerchantService.java
ª   ª                   ª       AdminTransactionService.java
ª   ª                   ª       BillPaymentService.java
ª   ª                   ª       ExternalTransferService.java
ª   ª                   ª       FraudDetectionService.java
ª   ª                   ª       InternalTransferService.java
ª   ª                   ª       MerchantPaymentService.java
ª   ª                   ª       ReconciliationService.java
ª   ª                   ª       ScheduledPaymentService.java
ª   ª                   ª       StandingInstructionService.java
ª   ª                   ª       TransactionApprovalService.java
ª   ª                   ª       TransactionExecutionService.java
ª   ª                   ª       TransactionFeeService.java
ª   ª                   ª       TransactionLimitService.java
ª   ª                   ª       TransactionNotificationService.java
ª   ª                   ª       TransactionReceiptService.java
ª   ª                   ª       TransactionReversalService.java
ª   ª                   ª       TransactionService.java
ª   ª                   ª       TransactionStatementService.java
ª   ª                   ª       TransactionValidationService.java
ª   ª                   ª       UpiCollectRequestService.java
ª   ª                   ª       UpiPinService.java
ª   ª                   ª       UpiQrService.java
ª   ª                   ª       UpiService.java
ª   ª                   ª       UpiTransactionService.java
ª   ª                   ª       
ª   ª                   +---util
ª   ª                   ª       TransactionIdGenerator.java
ª   ª                   ª       UpiQrCodeGenerator.java
ª   ª                   ª       
ª   ª                   +---validation
ª   ª                           BeneficiaryValidator.java
ª   ª                           TransactionValidator.java
ª   ª                           UpiValidator.java
ª   ª                           
ª   +---resources
ª       ª   application-dev.properties
ª       ª   application-prod.properties
ª       ª   application.properties
ª       ª   
ª       +---db
ª       ª   +---migration
ª       ª           V1__initial_schema.sql
ª       ª           V2__add_authentication_enhancements.sql
ª       ª           V3__fix_schema_constraints.sql
ª       ª           V4__transaction_module.sql
ª       ª           
ª       +---templates
ª           +---email
ª                   otp.html
ª                   password-reset.html
ª                   registration.html
ª                   security-alert.html
ª                   
+---test
    +---java
        +---com
            +---dvein
                +---banking_backend
                        BankingBackendApplicationTests.java
             
```

> Complete detailed structure available in the source project.

---

### Pending Items

1. RateLimit (Bug)
2. Session Device Status (Bug)
3. Document Media Support - Yet to be developed

---

## Setup Guide

### 1. Database Configuration

Open:

```properties
src/main/resources/application.properties
```

Update your database credentials:

```properties
spring.datasource.username=postgres
spring.datasource.password=password

```

### Create database
Open terminal:
Run these commends
```properties
>psql -U postgres
>postgres: CREATE DATABASE bankdb;
```


---

### 2. Email Configuration

Open:

```properties
src/main/resources/application.properties
```

Update:

```properties
spring.mail.username=onodaemailpodu123@gmail.com
spring.mail.password=onodapasswordpodu
```

#### Generate Gmail App Password

1. Enable **2-Step Verification** on your Google Account.
2. Visit:

https://myaccount.google.com/apppasswords

3. Generate a 16-digit App Password.
4. Replace:

```properties
spring.mail.password=YOUR_16_DIGIT_APP_PASSWORD
```

---

### 3. Postman API Documentation

#### Step 1

Run the backend application successfully.

#### Step 2

Open:

```text
http://localhost:8080/api/v1/api-docs
```

#### Step 3

Copy the entire JSON response.

#### Step 4

Open Postman:

- Click **Import**
- Paste the copied JSON

Postman will automatically generate all available API endpoints.

---

### 4. TOTP Testing

#### Prerequisites

Install **Google Authenticator** from Play Store.

#### Setup Process

##### Step 1

Call:

```http
POST http://localhost:8080/api/v1/totp/setup
```

##### Step 2

Response contains:

- Setup Key
- QR Code (Base64)

##### Step 3

Decode QR Base64:

https://base64.guru/converter/decode/image

##### Step 4

Scan QR using Google Authenticator.

##### Step 5

Use generated TOTP codes for verification APIs.

---

# Second Push Updates

## Database Improvements

* Integrated **FlywayDB** for database schema versioning and migration management.
* Ensures consistent database structure across development, testing, and production environments.
* Supports controlled and trackable database changes.

---

## Authentication Flow Redesign

### Previous Flow

```text
Login
↓
JWT Issued
↓
TOTP Verification
```

**Issue:**
A user could potentially receive an access token before completing Multi-Factor Authentication (MFA).

### New Secure Authentication Flow

#### Known Device

```text
Email + Password
↓
TOTP Verification
↓
Access Token Issued
```

#### New Device

```text
Email + Password
↓
Device Verification
↓
TOTP Verification
↓
Access Token Issued
```

### Authentication State Flow

```text
Login
↓
PRE_AUTH Token
↓
Device Verification
↓
TOTP Verification
↓
FULLY_AUTHENTICATED
↓
Access Token Issued
```

### Security Benefits

* Access tokens are no longer issued before MFA completion.
* Introduced **PRE_AUTH** authentication state.
* Device trust verification is enforced for new devices.
* Reduced risk of unauthorized account access.

---

## Enhanced Authentication Validation

### Previous Implementation

* Authentication relied primarily on **userId** validation.

### Current Implementation

* Authentication now validates both:

    * **Email**
    * **User ID**

### Benefits

* Additional identity verification layer.
* Reduced risk of user impersonation.
* Stronger authentication integrity.

---

## Bug Fixes

### User & Customer Mapping Fix

Resolved issues where:

* `userId`
* `customerId`

were not consistently handled across services and authentication flows.

Both identifiers now behave correctly throughout the application.

---

## Additional Service Layer Validation

Implemented extra validation checks across service-layer operations to:

* Prevent invalid request processing.
* Improve data integrity.
* Enforce stricter business rules.
* Enhance application security and reliability.

---

## Summary

### Added

* FlywayDB migration support.
* PRE_AUTH authentication mechanism.
* Device verification workflow.
* Additional service-layer validations.

### Improved

* MFA authentication flow.
* Identity verification using Email + User ID.
* Overall application security.
* Pre-Auth Token is Implemented
* RefreshToken is added in separate endpoint

### Fixed

* User ID and Customer ID mapping inconsistencies.


---

# Third Push Updates

## Security Hardening & Critical Bug Fixes

This update focuses on security improvements, authentication hardening, authorization fixes, session management enhancements, and multiple production bug fixes discovered during testing and review.

---

## Critical Security Fixes

### Secure Logout & Token Revocation

#### Issue

Previously, access tokens were blacklisted only when a valid `sessionId` was provided during logout.

#### Risk

Users could remain authenticated after logout under certain conditions.

#### Fix

* Logout now always blacklists the active access token.
* Token revocation is enforced regardless of session state.
* Eliminates token reuse after logout.

---

### Encryption Upgrade

#### Previous Implementation

```text
AES/ECB
```

#### Current Implementation

```text
AES/GCM
```

#### Improvements

* Authenticated encryption support.
* Random IV generated for every encryption operation.
* Protection against ciphertext manipulation.
* Eliminates ECB pattern leakage vulnerabilities.

---


### Authorization Hardening

#### IDOR (Insecure Direct Object Reference) Fixes

Resolved multiple authorization bypass vulnerabilities across:

* Account Services
* Credit Card Services

#### Improvements

* Ownership validation enforced.
* Database queries scoped to authenticated users.
* Prevents unauthorized access to other customer resources.

---

### Admin Endpoint Protection

#### Issue

`AdminCardController` endpoints were missing role enforcement.

#### Risk

Authenticated customers could potentially access card approval operations.

#### Fix

* Added `@RequireRole(ADMIN)` protection.
* Restricted approval and rejection workflows to administrators only.

---

### Password Change Security

#### Improvements

Changing a password now:

* Invalidates all active sessions.
* Revokes existing authentication tokens.
* Forces re-authentication on every device.

#### Benefit

Protects users from stolen or previously compromised sessions.

---

## Logic Bug Fixes

### Dashboard Metrics Correction

#### Issue

Approved credit card applications were incorrectly counted as pending.

#### Root Cause

Dashboard used:

```java
count()
```

instead of pending-only filtering.

#### Fix

```java
countByApprovedFalseAndRejectionReasonIsNull()
```

#### Result

Pending application statistics now display correctly.

---

### KYC Approval Workflow Fix

#### Issue

KYC approval could return:

```text
Customer not found
```

even for valid requests.

#### Root Cause

Admin `customerId` was incorrectly passed to:

```java
findByUserId()
```

#### Fix

Introduced:

```java
approveKycByCustomerId()
```

which correctly uses:

```java
findById(customerId)
```

#### Result

KYC approval process now works reliably.

---

### Session Response Fix

#### Issue

All sessions were incorrectly reported as:

```json
{
  "current": false
}
```

#### Root Cause

`userAgent` was not mapped into session responses.

#### Fix

Added:

```java
.userAgent(session.getUserAgent())
```

#### Result

Current device/session detection works correctly.

---

### Session Cleanup Scheduler Fix

#### Issue

Expired session cleanup job executed successfully but never removed sessions.

#### Root Cause

The scheduler queried:

```java
findByUserAndActiveTrue(null)
```

which always returned an empty result set.

#### Fix

Updated cleanup logic to correctly target active expired sessions.

#### Result

Expired sessions are now removed as expected.

---

### Monetary Validation Fix

#### Issue

`BigDecimal.equals(BigDecimal.ZERO)` failed when scales differed.

Example:

```java
new BigDecimal("0.00")
```

is not equal to:

```java
BigDecimal.ZERO
```

#### Fix

Replaced with:

```java
compareTo(BigDecimal.ZERO) != 0
```

#### Result

Reliable monetary comparisons regardless of scale.

---

### Async Processing Fix

#### Issue

`@Async` was applied to a private method.

#### Impact

Method execution remained synchronous and could block request threads.

#### Fix

Method visibility updated to allow Spring proxy interception.

#### Result

Async execution now functions correctly.

---

## Code Quality Improvements

### OTP Request Refactoring

Added dedicated DTO:

```java
ResendOtpRequest
```

#### Benefit

Removes misuse of unrelated request models and improves API clarity.

---

### Profile Update Validation

Added phone number uniqueness validation before database updates.

#### Benefits

* Prevents duplicate phone numbers.
* Avoids unnecessary database writes.
* Improves validation feedback.

---

### Exception Handling Enhancements

Added global handling for:

```java
DataIntegrityViolationException
```

and

```java
InvalidRequestException
```

#### Benefits

* Consistent API error responses.
* Improved debugging and client-side handling.

---

### Authentication Error Handling

#### Previous Behavior

```java
RuntimeException
```

could return HTTP 500 responses for authentication failures.

#### Current Behavior

```java
UnauthorizedException
```

returns:

```http
401 Unauthorized
```

#### Benefit

More accurate API semantics and improved client handling.

---

### Dead Code Cleanup

Removed duplicate:

```java
approveCreditCardApplication()
```

implementation.

#### Benefits

* Reduced maintenance overhead.
* Cleaner service layer.

---

### Rate Limiting Restored

Rate limiting protections have been re-enabled across protected endpoints.

#### Benefits

* Reduces abuse and brute-force attempts.
* Improves API stability.
* Enhances overall security posture.

---

## Summary

### Security

* Access token revocation on every logout.
* AES/GCM encryption implementation.
* Environment-based secret management.
* IDOR vulnerability fixes.
* Admin endpoint protection.
* Session invalidation after password change.

### Fixed

* Dashboard pending count bug.
* KYC approval customer lookup bug.
* Session current-device detection bug.
* Session cleanup scheduler issue.
* BigDecimal comparison issue.
* Async execution issue.

### Improved

* Exception handling.
* Request validation.
* DTO design.
* Authentication error responses.
* Rate limiting enforcement.
* Codebase maintainability.

## Final Notes

* All tests are verified and core business logic is working successfully.
* Thereby my assigned job and module is completed and verified successfully.
