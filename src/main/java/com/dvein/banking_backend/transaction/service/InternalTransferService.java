package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.transaction.dto.request.InternalTransferRequest;
import com.dvein.banking_backend.transaction.dto.response.TransactionResponse;
import com.dvein.banking_backend.transaction.enums.*;
import com.dvein.banking_backend.transaction.exception.FraudDetectedException;
import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.model.TransactionCategory;
import com.dvein.banking_backend.transaction.model.TransactionLimit;
import com.dvein.banking_backend.transaction.repository.TransactionCategoryRepository;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import com.dvein.banking_backend.transaction.util.TransactionIdGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalTransferService {

    private final TransactionValidationService validationService;
    private final TransactionExecutionService executionService;
    private final TransactionFeeService feeService;
    private final TransactionLimitService limitService;
    private final FraudDetectionService fraudDetectionService;
    private final TransactionNotificationService notificationService;
    private final TransactionRepository transactionRepository;
    private final TransactionCategoryRepository categoryRepository;
    private final TransactionIdGenerator idGenerator;

    @Transactional
    public TransactionResponse transferMoney(InternalTransferRequest request, String email, HttpServletRequest httpRequest) {
        // Validate idempotency
        validationService.validateIdempotency(request.getIdempotencyKey());

        // Get and validate sender account
        Account senderAccount = validationService.getAndValidateSenderAccount(request.getSenderAccountId(), email);

        // Get and validate receiver account
        Account receiverAccount = validationService.getAndValidateReceiverAccountByNumber(request.getReceiverAccountNumber());

        // Validate not same account
        validationService.validateNotSameAccount(senderAccount, receiverAccount);

        // Validate customer status and KYC
        validationService.validateCustomerStatus(senderAccount.getCustomer());
        validationService.validateKycStatus(senderAccount.getCustomer());

        // Validate amount
        validationService.validateTransactionAmount(request.getAmount(), BigDecimal.ONE, null);

        // Calculate fees (Internal transfer is free)
        TransactionFeeService.FeeCalculation feeCalc = feeService.calculateFee(
                TransactionType.INTERNAL_TRANSFER, request.getAmount());
        BigDecimal totalAmount = request.getAmount().add(feeCalc.getTotalFee());

        // Validate balance
        validationService.validateSufficientBalance(senderAccount, totalAmount);

        // Get transaction category
        TransactionCategory category = categoryRepository.findByName("TRANSFER").orElse(null);

        // Create transaction
        Transaction transaction = Transaction.builder()
                .transactionId(idGenerator.generateTransactionId())
                .idempotencyKey(request.getIdempotencyKey())
                .senderAccount(senderAccount)
                .receiverAccount(receiverAccount)
                .receiverName(receiverAccount.getCustomer().getFullName())
                .amount(request.getAmount())
                .transactionType(TransactionType.INTERNAL_TRANSFER)
                .transactionMode(TransactionMode.ONLINE)
                .paymentMethod(PaymentMethod.ACCOUNT_TRANSFER)
                .status(TransactionStatus.INITIATED)
                .category(category)
                .description(request.getDescription())
                .remarks(request.getRemarks())
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
        limitService.validateAndUpdateLimit(limit, TransactionType.INTERNAL_TRANSFER, totalAmount);

        // Execute transaction
        executionService.executeTransaction(transaction);

        // Send notifications
        notificationService.sendTransactionNotification(transaction);

        log.info("Internal transfer completed: {} from {} to {}",
                transaction.getTransactionId(),
                senderAccount.getAccountNumber(),
                receiverAccount.getAccountNumber());

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
                        transaction.getReceiverAccount().getAccountNumber() :
                        transaction.getReceiverAccountNumber())
                .receiverName(transaction.getReceiverName())
                .receiverBankName(transaction.getReceiverBankName())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .transactionType(transaction.getTransactionType())
                .transactionMode(transaction.getTransactionMode())
                .paymentMethod(transaction.getPaymentMethod())
                .status(transaction.getStatus())
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .description(transaction.getDescription())
                .remarks(transaction.getRemarks())
                .referenceNumber(transaction.getReferenceNumber())
                .utrNumber(transaction.getUtrNumber())
                .transactionFee(transaction.getTransactionFee())
                .gst(transaction.getGst())
                .totalAmount(transaction.getTotalAmount())
                .senderBalanceBefore(transaction.getSenderBalanceBefore())
                .senderBalanceAfter(transaction.getSenderBalanceAfter())
                .initiatedAt(transaction.getInitiatedAt())
                .completedAt(transaction.getCompletedAt())
                .failedAt(transaction.getFailedAt())
                .failureReason(transaction.getFailureReason())
                .flagged(transaction.isFlagged())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}