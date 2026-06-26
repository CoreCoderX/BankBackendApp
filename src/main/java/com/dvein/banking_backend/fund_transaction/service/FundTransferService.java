package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.notification.service.EmailService;
import com.dvein.banking_backend.transaction.dto.request.AccountTransferRequest;
import com.dvein.banking_backend.transaction.dto.request.BeneficiaryTransferRequest;
import com.dvein.banking_backend.transaction.dto.request.SelfTransferRequest;
import com.dvein.banking_backend.transaction.dto.response.TransactionResponse;
import com.dvein.banking_backend.transaction.enums.PaymentChannel;
import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.enums.TransactionType;
import com.dvein.banking_backend.transaction.mapper.TransactionMapper;
import com.dvein.banking_backend.transaction.model.Beneficiary;
import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.repository.BeneficiaryRepository;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import com.dvein.banking_backend.transaction.validator.TransferValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class FundTransferService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final LimitService limitService;
    private final FraudDetectionService fraudDetectionService;
    private final IdempotencyService idempotencyService;
    private final TransferValidator transferValidator;
    private final TransactionMapper transactionMapper;
    private final EmailService emailService;
    private final HttpServletRequest httpServletRequest;

    @Transactional
    public ApiResponse<TransactionResponse> selfTransfer(SelfTransferRequest request) {
        log.info("Processing self transfer request");

        // Check idempotency
        idempotencyService.checkIdempotency(request.getIdempotencyKey());

        Long userId = SecurityContextHelper.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate accounts
        Account senderAccount = accountRepository.findById(request.getSenderAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender account not found"));

        Account receiverAccount = accountRepository.findById(request.getReceiverAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));

        // Validate ownership
        transferValidator.validateAccountOwnership(senderAccount, userId);
        transferValidator.validateAccountOwnership(receiverAccount, userId);

        // Validate accounts are different
        if (senderAccount.getId().equals(receiverAccount.getId())) {
            throw new InvalidRequestException("Cannot transfer to the same account");
        }

        // Validate balance
        transferValidator.validateBalance(senderAccount, request.getAmount());

        // Validate limits
        limitService.validateTransferLimit(userId, request.getAmount());

        // Fraud detection
        fraudDetectionService.checkFraudRisk(userId, request.getAmount());

        // Create transaction
        Transaction transaction = buildTransaction(
                user,
                senderAccount,
                receiverAccount,
                request.getAmount(),
                request.getRemarks(),
                TransactionType.SELF_TRANSFER
        );

        transaction.setStatus(TransactionStatus.INITIATED);

        try {
            // Execute transfer
            executeTransfer(senderAccount, receiverAccount, request.getAmount());

            transaction.setStatus(TransactionStatus.SUCCESS);
            Transaction savedTransaction = transactionRepository.save(transaction);

            // Update limits
            limitService.updateTransferLimit(userId, request.getAmount());

            // Save idempotency
            idempotencyService.saveIdempotency(
                    request.getIdempotencyKey(),
                    TransactionStatus.SUCCESS,
                    savedTransaction.getTransactionId()
            );

            // Send notifications
            sendTransferNotifications(user, savedTransaction);

            log.info("Self transfer completed successfully: {}", savedTransaction.getTransactionId());

            return ApiResponse.success(
                    "Transfer completed successfully",
                    transactionMapper.toResponse(savedTransaction)
            );

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            idempotencyService.saveIdempotency(
                    request.getIdempotencyKey(),
                    TransactionStatus.FAILED,
                    transaction.getTransactionId()
            );

            log.error("Self transfer failed", e);
            throw new InvalidRequestException("Transfer failed: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<TransactionResponse> accountTransfer(AccountTransferRequest request) {
        log.info("Processing account transfer request");

        // Check idempotency
        idempotencyService.checkIdempotency(request.getIdempotencyKey());

        Long userId = SecurityContextHelper.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate sender account
        Account senderAccount = accountRepository.findById(request.getSenderAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender account not found"));

        transferValidator.validateAccountOwnership(senderAccount, userId);

        // Find receiver account
        Account receiverAccount = accountRepository.findByAccountNumber(request.getReceiverAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));

        // Validate accounts are different
        if (senderAccount.getAccountNumber().equals(receiverAccount.getAccountNumber())) {
            throw new InvalidRequestException("Cannot transfer to the same account");
        }

        // Validate balance
        transferValidator.validateBalance(senderAccount, request.getAmount());

        // Validate limits
        limitService.validateTransferLimit(userId, request.getAmount());

        // Fraud detection
        fraudDetectionService.checkFraudRisk(userId, request.getAmount());

        // Create transaction
        Transaction transaction = buildTransaction(
                user,
                senderAccount,
                receiverAccount,
                request.getAmount(),
                request.getRemarks(),
                TransactionType.ACCOUNT_TRANSFER
        );

        transaction.setStatus(TransactionStatus.INITIATED);

        try {
            // Execute transfer
            executeTransfer(senderAccount, receiverAccount, request.getAmount());

            transaction.setStatus(TransactionStatus.SUCCESS);
            Transaction savedTransaction = transactionRepository.save(transaction);

            // Update limits
            limitService.updateTransferLimit(userId, request.getAmount());

            // Save idempotency
            idempotencyService.saveIdempotency(
                    request.getIdempotencyKey(),
                    TransactionStatus.SUCCESS,
                    savedTransaction.getTransactionId()
            );

            // Send notifications
            sendTransferNotifications(user, savedTransaction);

            log.info("Account transfer completed successfully: {}", savedTransaction.getTransactionId());

            return ApiResponse.success(
                    "Transfer completed successfully",
                    transactionMapper.toResponse(savedTransaction)
            );

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            idempotencyService.saveIdempotency(
                    request.getIdempotencyKey(),
                    TransactionStatus.FAILED,
                    transaction.getTransactionId()
            );

            log.error("Account transfer failed", e);
            throw new InvalidRequestException("Transfer failed: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<TransactionResponse> beneficiaryTransfer(BeneficiaryTransferRequest request) {
        log.info("Processing beneficiary transfer request");

        // Check idempotency
        idempotencyService.checkIdempotency(request.getIdempotencyKey());

        Long userId = SecurityContextHelper.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate sender account
        Account senderAccount = accountRepository.findById(request.getSenderAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender account not found"));

        transferValidator.validateAccountOwnership(senderAccount, userId);

        // Validate beneficiary
        Beneficiary beneficiary = beneficiaryRepository.findByIdAndUserId(request.getBeneficiaryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));

        if (!beneficiary.getIsActive()) {
            throw new InvalidRequestException("Beneficiary is not active");
        }

        // Find receiver account
        Account receiverAccount = accountRepository.findByAccountNumber(beneficiary.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));

        // Validate balance
        transferValidator.validateBalance(senderAccount, request.getAmount());

        // Validate limits
        limitService.validateTransferLimit(userId, request.getAmount());

        // Fraud detection
        fraudDetectionService.checkFraudRisk(userId, request.getAmount());

        // Create transaction
        Transaction transaction = buildTransaction(
                user,
                senderAccount,
                receiverAccount,
                request.getAmount(),
                request.getRemarks(),
                TransactionType.BENEFICIARY_TRANSFER
        );

        transaction.setStatus(TransactionStatus.INITIATED);

        try {
            // Execute transfer
            executeTransfer(senderAccount, receiverAccount, request.getAmount());

            transaction.setStatus(TransactionStatus.SUCCESS);
            Transaction savedTransaction = transactionRepository.save(transaction);

            // Update limits
            limitService.updateTransferLimit(userId, request.getAmount());

            // Save idempotency
            idempotencyService.saveIdempotency(
                    request.getIdempotencyKey(),
                    TransactionStatus.SUCCESS,
                    savedTransaction.getTransactionId()
            );

            // Send notifications
            sendTransferNotifications(user, savedTransaction);

            log.info("Beneficiary transfer completed successfully: {}", savedTransaction.getTransactionId());

            return ApiResponse.success(
                    "Transfer completed successfully",
                    transactionMapper.toResponse(savedTransaction)
            );

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            idempotencyService.saveIdempotency(
                    request.getIdempotencyKey(),
                    TransactionStatus.FAILED,
                    transaction.getTransactionId()
            );

            log.error("Beneficiary transfer failed", e);
            throw new InvalidRequestException("Transfer failed: " + e.getMessage());
        }
    }

    private void executeTransfer(Account senderAccount, Account receiverAccount, BigDecimal amount) {
        // Debit sender
        BigDecimal newSenderBalance = senderAccount.getBalance().subtract(amount);
        senderAccount.setBalance(newSenderBalance);
        accountRepository.save(senderAccount);

        // Credit receiver
        BigDecimal newReceiverBalance = receiverAccount.getBalance().add(amount);
        receiverAccount.setBalance(newReceiverBalance);
        accountRepository.save(receiverAccount);
    }

    private Transaction buildTransaction(
            User user,
            Account senderAccount,
            Account receiverAccount,
            BigDecimal amount,
            String remarks,
            TransactionType type
    ) {
        return Transaction.builder()
                .user(user)
                .senderAccount(senderAccount)
                .receiverAccount(receiverAccount)
                .amount(amount)
                .remarks(remarks)
                .transactionType(type)
                .channel(PaymentChannel.WEB)
                .ipAddress(getClientIp())
                .deviceInfo(getUserAgent())
                .createdBy(user.getEmail())
                .build();
    }

    private void sendTransferNotifications(User user, Transaction transaction) {
        try {
            // Send debit notification
            emailService.sendEmail(
                    user.getEmail(),
                    "Debit Alert - ₹" + transaction.getAmount(),
                    buildDebitEmailContent(transaction)
            );

            // Send credit notification to receiver (if they are also a user)
            if (transaction.getReceiverAccount().getCustomer() != null &&
                    transaction.getReceiverAccount().getCustomer().getUser() != null) {

                User receiverUser = transaction.getReceiverAccount().getCustomer().getUser();
                emailService.sendEmail(
                        receiverUser.getEmail(),
                        "Credit Alert - ₹" + transaction.getAmount(),
                        buildCreditEmailContent(transaction)
                );
            }
        } catch (Exception e) {
            log.error("Failed to send transfer notifications", e);
        }
    }

    private String buildDebitEmailContent(Transaction transaction) {
        return String.format("""
            Dear Customer,
            
            Your account %s has been debited with ₹%s.
            
            Transaction Details:
            Transaction ID: %s
            Reference Number: %s
            Amount: ₹%s
            To Account: %s
            Date: %s
            Remarks: %s
            
            Available Balance: ₹%s
            
            If you did not authorize this transaction, please contact us immediately.
            
            Thank you for banking with us.
            """,
                maskAccountNumber(transaction.getSenderAccount().getAccountNumber()),
                transaction.getAmount(),
                transaction.getTransactionId(),
                transaction.getReferenceNumber(),
                transaction.getAmount(),
                maskAccountNumber(transaction.getReceiverAccount().getAccountNumber()),
                transaction.getCreatedAt(),
                transaction.getRemarks() != null ? transaction.getRemarks() : "N/A",
                transaction.getSenderAccount().getBalance()
        );
    }

    private String buildCreditEmailContent(Transaction transaction) {
        return String.format("""
            Dear Customer,
            
            Your account %s has been credited with ₹%s.
            
            Transaction Details:
            Transaction ID: %s
            Reference Number: %s
            Amount: ₹%s
            From Account: %s
            Date: %s
            Remarks: %s
            
            Available Balance: ₹%s
            
            Thank you for banking with us.
            """,
                maskAccountNumber(transaction.getReceiverAccount().getAccountNumber()),
                transaction.getAmount(),
                transaction.getTransactionId(),
                transaction.getReferenceNumber(),
                transaction.getAmount(),
                maskAccountNumber(transaction.getSenderAccount().getAccountNumber()),
                transaction.getCreatedAt(),
                transaction.getRemarks() != null ? transaction.getRemarks() : "N/A",
                transaction.getReceiverAccount().getBalance()
        );
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        int length = accountNumber.length();
        return "XXXX" + accountNumber.substring(length - 4);
    }

    private String getClientIp() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return httpServletRequest.getRemoteAddr();
    }

    private String getUserAgent() {
        return httpServletRequest.getHeader("User-Agent");
    }
}