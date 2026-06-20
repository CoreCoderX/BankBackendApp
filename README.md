# Banking Backend Project

## Module - 1 (Sivaprakash)

### Module Breakdown

### 1. Auth Module

```text
auth/
├── controller (6)
├── dto/request (12)
├── dto/response (6)
├── model (10)
├── repository (10)
└── service (8)
```

#### Features

- Registration/Login
- OTP
- Password Reset
- MPIN
- TOTP
- Device Management
- Session Management
- Login History

---

### 2. Account Module

```text
account/
├── controller (7)
├── model (7)
├── repository (7)
└── service (7)
```

#### Features

- Customer Profile
- Bank Accounts
- Beneficiaries
- Nominees
- Documents
- KYC
- Verification APIs

---

### 3. Card Module

```text
card/
├── controller (2)
├── model (3)
├── repository (3)
└── service (2)
```

#### Features

- Debit Cards
- Credit Cards
- PIN Management
- Card Security

---

### 4. Admin Module

```text
admin/
├── controller (5)
├── model (1)
├── repository (1)
└── service (4)
```

#### Features

- Admin Authentication
- Customer Management
- KYC Approval
- Credit Card Approval
- Dashboard
- Audit Logs

---

### 5. Common Module

```text
common/
├── security
├── config
├── exception
├── annotation
├── aspect
├── util
├── enums
├── dto
└── constants
```

---

## Final Folder Structure

```text
Folder PATH listing for volume Windows-SSD
Volume serial number is C20D-95B5
C:\USERS\SIVAP\DOWNLOADS\BANKING-BACKEND\SRC
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
ª   ª               ª   ª   ª       RegisterDeviceRequest.java
ª   ª               ª   ª   ª       RegisterRequest.java
ª   ª               ª   ª   ª       ResetPasswordRequest.java
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
ª   ª               ª   ª       Role.java
ª   ª               ª   ª       RolePermission.java
ª   ª               ª   ª       Session.java
ª   ª               ª   ª       TotpSecret.java
ª   ª               ª   ª       User.java
ª   ª               ª   ª       
ª   ª               ª   +---repository
ª   ª               ª   ª       DeviceRepository.java
ª   ª               ª   ª       LoginHistoryRepository.java
ª   ª               ª   ª       MpinRepository.java
ª   ª               ª   ª       OtpRepository.java
ª   ª               ª   ª       PermissionRepository.java
ª   ª               ª   ª       RolePermissionRepository.java
ª   ª               ª   ª       RoleRepository.java
ª   ª               ª   ª       SessionRepository.java
ª   ª               ª   ª       TotpSecretRepository.java
ª   ª               ª   ª       UserRepository.java
ª   ª               ª   ª       
ª   ª               ª   +---service
ª   ª               ª           AuthService.java
ª   ª               ª           BiometricService.java
ª   ª               ª           DeviceService.java
ª   ª               ª           LoginHistoryService.java
ª   ª               ª           MpinService.java
ª   ª               ª           OtpService.java
ª   ª               ª           SessionService.java
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
ª   ª               ª   ª       SecurityConfig.java
ª   ª               ª   ª       
ª   ª               ª   +---constant
ª   ª               ª   ª       AppConstants.java
ª   ª               ª   ª       ErrorCodes.java
ª   ª               ª   ª       SuccessMessages.java
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
ª   ª                   +---service
ª   ª                   ª       EmailService.java
ª   ª                   ª       
ª   ª                   +---template
ª   ª                           EmailTemplates.java
ª   ª                           
ª   +---resources
ª       ª   application-dev.properties
ª       ª   application-prod.properties
ª       ª   application.properties
ª       ª   
ª       +---db
ª       ª   +---migration
ª       ª           V1__initial_schema.sql
ª       ª           
ª       +---static
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

## Notes

All tests are verified and core business logic is working successfully.

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
>postgres: CREATE DATABASE bankdb
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

## Module - 2 (Yeshwanth)

- Will be written by Yeshwanth

---

## Module - 3 (Ajai)

- Will be written by Ajai

---

## Module - 4 (Novin Kumar)

- Will be written by Novin Kumar

---

## Contributors

| Module | Owner |
|----------|----------|
| Module 1 | Sivaprakash |
| Module 2 | Yeshwanth |
| Module 3 | Ajai |
| Module 4 | Novin Kumar |
