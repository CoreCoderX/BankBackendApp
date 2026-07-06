package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.transaction.dto.request.ScanQrRequest;
import com.dvein.banking_backend.transaction.dto.request.UpiSendMoneyRequest;
import com.dvein.banking_backend.transaction.dto.response.TransactionResponse;
import com.dvein.banking_backend.transaction.enums.*;
import com.dvein.banking_backend.transaction.exception.FraudDetectedException;
import com.dvein.banking_backend.transaction.exception.InvalidUpiIdException;
import com.dvein.banking_backend.transaction.model.*;
import com.dvein.banking_backend.transaction.repository.*;
import com.dvein.banking_backend.transaction.util.TransactionIdGenerator;
import com.dvein.banking_backend.transaction.validation.UpiValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpiTransactionService {

    private final UpiService upiService;
    private final UpiPinService upiPinService;
    private final UpiQrService upiQrService;
    private final TransactionValidationService validationService;
    private final TransactionExecutionService executionService;
    private final TransactionFeeService feeService;
    private final TransactionLimitService limitService;
    private final FraudDetectionService fraudDetectionService;
    private final TransactionNotificationService notificationService;
    private final TransactionRepository transactionRepository;
    private final UpiTransactionRepository upiTransactionRepository;
    private final UpiIdRepository upiIdRepository;
    private final TransactionCategoryRepository categoryRepository;
    private final TransactionIdGenerator idGenerator;
    private final UpiValidator upiValidator;

    @Transactional
    public TransactionResponse sendMoney(UpiSendMoneyRequest request, String email, HttpServletRequest httpRequest) {
        // Validate idempotency
        validationService.validateIdempotency(request.getIdempotencyKey());

        // Validate UPI IDs
        upiValidator.validateUpiIdFormat(request.getSenderUpiId());
        upiValidator.validateUpiIdFormat(request.getReceiverUpiId());

        if (request.getSenderUpiId().equals(request.getReceiverUpiId())) {
            throw new InvalidRequestException("Cannot send money to yourself");
        }

        // Get sender UPI ID
        UpiId senderUpiId = upiService.getUpiIdByUpiIdString(request.getSenderUpiId());
        if (!senderUpiId.getUpiProfile().getCustomer().getUser().getEmail().equals(email)) {
            throw new InvalidRequestException("You do not own the sender UPI ID");
        }

        upiValidator.validateUpiIdActive(senderUpiId);

        // Verify UPI PIN
        upiPinService.verifyUpiPinByProfile(senderUpiId.getUpiProfile(), request.getUpiPin());

        // Get receiver UPI ID
        UpiId receiverUpiId = upiIdRepository.findByUpiId(request.getReceiverUpiId())
                .orElseThrow(() -> new InvalidUpiIdException("Receiver UPI ID not found"));

        upiValidator.validateUpiIdActive(receiverUpiId);

        // Get accounts
        Account senderAccount = senderUpiId.getLinkedAccount();
        Account receiverAccount = receiverUpiId.getLinkedAccount();

        // Validate accounts
        validationService.validateSenderAccount(senderAccount);
        validationService.validateReceiverAccount(receiverAccount);
        validationService.validateCustomerStatus(senderAccount.getCustomer());
        validationService.validateKycStatus(senderAccount.getCustomer());
        validationService.validateNotSameAccount(senderAccount, receiverAccount);

        // Validate amount and limits
        validationService.validateTransactionAmount(request.getAmount(), BigDecimal.ONE, BigDecimal.valueOf(100000));

        // Calculate fees (UPI is free)
        TransactionFeeService.FeeCalculation feeCalc = feeService.calculateFee(TransactionType.UPI_TRANSFER, request.getAmount());
        BigDecimal totalAmount = request.getAmount().add(feeCalc.getTotalFee());

        // Validate balance
        validationService.validateSufficientBalance(senderAccount, totalAmount);

        // Get transaction category
        TransactionCategory category = categoryRepository.findByName("UPI")
                .orElse(null);

        // Create transaction
        Transaction transaction = Transaction.builder()
                .transactionId(idGenerator.generateTransactionId())
                .idempotencyKey(request.getIdempotencyKey())
                .senderAccount(senderAccount)
                .receiverAccount(receiverAccount)
                .amount(request.getAmount())
                .transactionType(TransactionType.UPI_TRANSFER)
                .transactionMode(TransactionMode.ONLINE)
                .paymentMethod(PaymentMethod.UPI)
                .status(TransactionStatus.INITIATED)
                .category(category)
                .description(request.getDescription())
                .transactionFee(feeCalc.getBaseFee())
                .gst(feeCalc.getGst())
                .totalAmount(totalAmount)
                .ipAddress(getClientIp(httpRequest))
                .deviceId(httpRequest.getHeader("X-Device-ID"))
                .build();

        transaction = transactionRepository.save(transaction);

        // Fraud detection
        Customer customer = senderAccount.getCustomer();
        FraudDetectionService.FraudCheckResult fraudCheck = fraudDetectionService.checkFraud(transaction, customer);

        transaction.setFraudScore(fraudCheck.getRiskScore());
        if (fraudCheck.isShouldBlock()) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Transaction blocked due to suspicious activity");
            transaction.setFlagged(true);
            transactionRepository.save(transaction);

            notificationService.sendFraudAlert(email, transaction.getTransactionId(), fraudCheck.getDetails());
            throw new FraudDetectedException("Transaction blocked due to fraud detection");
        }

        // Update transaction limits
        TransactionLimit limit = limitService.getOrCreateLimit(customer);
        limitService.validateAndUpdateLimit(limit, TransactionType.UPI_TRANSFER, totalAmount);

        // Create UPI transaction record
        UpiTransaction upiTransaction = UpiTransaction.builder()
                .transaction(transaction)
                .senderUpiId(request.getSenderUpiId())
                .receiverUpiId(request.getReceiverUpiId())
                .vpaVerified(true)
                .build();

        upiTransactionRepository.save(upiTransaction);

        // Execute transaction
        executionService.executeTransaction(transaction);

        // Send notifications
        notificationService.sendTransactionNotification(transaction);

        log.info("UPI transaction completed: {} from {} to {}",
                transaction.getTransactionId(), request.getSenderUpiId(), request.getReceiverUpiId());

        return mapToTransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse payViaQr(ScanQrRequest request, String email, HttpServletRequest httpRequest) {
        // Validate idempotency
        validationService.validateIdempotency(request.getIdempotencyKey());

        // Validate and parse QR code
        UpiQrCode qrCode = upiQrService.validateQrCode(request.getQrData());

        if (!qrCode.isActive()) {
            throw new InvalidRequestException("QR code is not active");
        }

        if (qrCode.isExpired()) {
            throw new InvalidRequestException("QR code has expired");
        }

        if (qrCode.isMaxScansReached()) {
            throw new InvalidRequestException("QR code has reached maximum scan limit");
        }

        // Get payer UPI ID
        UpiId payerUpiId = upiService.getUpiIdByUpiIdString(request.getPayerUpiId());
        if (!payerUpiId.getUpiProfile().getCustomer().getUser().getEmail().equals(email)) {
            throw new InvalidRequestException("You do not own the payer UPI ID");
        }

        upiValidator.validateUpiIdActive(payerUpiId);

        // Verify UPI PIN
        upiPinService.verifyUpiPinByProfile(payerUpiId.getUpiProfile(), request.getUpiPin());

        // Get payee UPI ID
        UpiId payeeUpiId = upiIdRepository.findByUpiId(qrCode.getUpiId())
                .orElseThrow(() -> new InvalidUpiIdException("Payee UPI ID not found"));

        if (request.getPayerUpiId().equals(qrCode.getUpiId())) {
            throw new InvalidRequestException("Cannot pay to yourself");
        }

        Account payerAccount = payerUpiId.getLinkedAccount();
        Account payeeAccount = payeeUpiId.getLinkedAccount();

        validationService.validateSenderAccount(payerAccount);
        validationService.validateReceiverAccount(payeeAccount);
        validationService.validateCustomerStatus(payerAccount.getCustomer());
        validationService.validateKycStatus(payerAccount.getCustomer());

        BigDecimal amount = qrCode.getAmount(); // Amount from QR code
        if (amount == null) {
            throw new InvalidRequestException("QR code does not contain amount");
        }

        TransactionFeeService.FeeCalculation feeCalc = feeService.calculateFee(TransactionType.UPI_TRANSFER, amount);
        BigDecimal totalAmount = amount.add(feeCalc.getTotalFee());

        validationService.validateSufficientBalance(payerAccount, totalAmount);

        TransactionCategory category = categoryRepository.findByName("UPI").orElse(null);

        Transaction transaction = Transaction.builder()
                .transactionId(idGenerator.generateTransactionId())
                .idempotencyKey(request.getIdempotencyKey())
                .senderAccount(payerAccount)
                .receiverAccount(payeeAccount)
                .amount(amount)
                .transactionType(TransactionType.UPI_TRANSFER)
                .transactionMode(TransactionMode.ONLINE)
                .paymentMethod(PaymentMethod.QR_CODE)
                .status(TransactionStatus.INITIATED)
                .category(category)
                .description(qrCode.getDescription())
                .transactionFee(feeCalc.getBaseFee())
                .gst(feeCalc.getGst())
                .totalAmount(totalAmount)
                .ipAddress(getClientIp(httpRequest))
                .deviceId(httpRequest.getHeader("X-Device-ID"))
                .build();

        transaction = transactionRepository.save(transaction);

        // Fraud detection
        Customer customer = payerAccount.getCustomer();
        FraudDetectionService.FraudCheckResult fraudCheck = fraudDetectionService.checkFraud(transaction, customer);

        transaction.setFraudScore(fraudCheck.getRiskScore());
        if (fraudCheck.isShouldBlock()) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Transaction blocked");
            transaction.setFlagged(true);
            transactionRepository.save(transaction);

            notificationService.sendFraudAlert(email, transaction.getTransactionId(), fraudCheck.getDetails());
            throw new FraudDetectedException("Transaction blocked");
        }

        TransactionLimit limit = limitService.getOrCreateLimit(customer);
        limitService.validateAndUpdateLimit(limit, TransactionType.UPI_TRANSFER, totalAmount);

        UpiTransaction upiTransaction = UpiTransaction.builder()
                .transaction(transaction)
                .senderUpiId(request.getPayerUpiId())
                .receiverUpiId(qrCode.getUpiId())
                .vpaVerified(true)
                .qrCode(qrCode.getQrId() != null ? qrCode : null)
                .build();

        upiTransactionRepository.save(upiTransaction);

        // Increment QR scan count if it's a saved QR
        if (qrCode.getQrId() != null) {
            upiQrService.incrementScanCount(qrCode);
        }

        executionService.executeTransaction(transaction);
        notificationService.sendTransactionNotification(transaction);

        log.info("QR payment completed: {} from {} to {}",
                transaction.getTransactionId(), request.getPayerUpiId(), qrCode.getUpiId());

        return mapToTransactionResponse(transaction);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .senderAccountNumber(transaction.getSenderAccount() != null ?
                        transaction.getSenderAccount().getAccountNumber() : null)
                .receiverAccountNumber(transaction.getReceiverAccount() != null ?
                        transaction.getReceiverAccount().getAccountNumber() : null)
                .receiverName(transaction.getReceiverName())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .transactionType(transaction.getTransactionType())
                .transactionMode(transaction.getTransactionMode())
                .paymentMethod(transaction.getPaymentMethod())
                .status(transaction.getStatus())
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .description(transaction.getDescription())
                .transactionFee(transaction.getTransactionFee())
                .gst(transaction.getGst())
                .totalAmount(transaction.getTotalAmount())
                .initiatedAt(transaction.getInitiatedAt())
                .completedAt(transaction.getCompletedAt())
                .failedAt(transaction.getFailedAt())
                .failureReason(transaction.getFailureReason())
                .flagged(transaction.isFlagged())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}