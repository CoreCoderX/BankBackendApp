package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.transaction.dto.request.ExternalTransferRequest;
import com.dvein.banking_backend.transaction.dto.response.TransactionResponse;
import com.dvein.banking_backend.transaction.enums.*;
import com.dvein.banking_backend.transaction.exception.FraudDetectedException;
import com.dvein.banking_backend.transaction.model.Bank;
import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.model.TransactionCategory;
import com.dvein.banking_backend.transaction.model.TransactionLimit;
import com.dvein.banking_backend.transaction.repository.BankRepository;
import com.dvein.banking_backend.transaction.repository.TransactionCategoryRepository;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import com.dvein.banking_backend.transaction.util.TransactionIdGenerator;
import com.dvein.banking_backend.transaction.validation.TransactionValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalTransferService {

    private final TransactionValidationService validationService;
    private final TransactionExecutionService executionService;
    private final TransactionFeeService feeService;
    private final TransactionLimitService limitService;
    private final FraudDetectionService fraudDetectionService;
    private final TransactionNotificationService notificationService;
    private final TransactionRepository transactionRepository;
    private final TransactionCategoryRepository categoryRepository;
    private final BankRepository bankRepository;
    private final TransactionIdGenerator idGenerator;
    private final TransactionValidator transactionValidator;
    private final AccountRepository accountRepository;

    @Transactional
    public TransactionResponse transferMoney(ExternalTransferRequest request, String email, HttpServletRequest httpRequest) {
        // Validate idempotency
        validationService.validateIdempotency(request.getIdempotencyKey());

        // Validate IFSC code
        transactionValidator.validateIfscCode(request.getIfscCode());

        // Get sender account
        Account senderAccount = validationService.getAndValidateSenderAccount(request.getSenderAccountId(), email);

        // Validate customer
        validationService.validateCustomerStatus(senderAccount.getCustomer());
        validationService.validateKycStatus(senderAccount.getCustomer());

        // Determine transaction type and validate amount
        TransactionType transactionType = determineTransferType(request.getTransferMode());
        validateTransferModeAndAmount(transactionType, request.getAmount());

        // Calculate fees
        TransactionFeeService.FeeCalculation feeCalc = feeService.calculateFee(transactionType, request.getAmount());
        BigDecimal totalAmount = request.getAmount().add(feeCalc.getTotalFee());

        // Validate balance
        validationService.validateSufficientBalance(senderAccount, totalAmount);

        // Get bank details
        String ifscPrefix = request.getIfscCode().substring(0, 4);
        Bank bank = bankRepository.findByIfscPrefix(ifscPrefix).orElse(null);
        String bankName = bank != null ? bank.getBankName() : "External Bank";

        // Get category
        TransactionCategory category = categoryRepository.findByName("TRANSFER").orElse(null);

        // Create transaction
        Transaction transaction = Transaction.builder()
                .transactionId(idGenerator.generateTransactionId())
                .idempotencyKey(request.getIdempotencyKey())
                .senderAccount(senderAccount)
                .receiverAccount(null) // External transfer
                .receiverAccountNumber(request.getReceiverAccountNumber())
                .receiverIfscCode(request.getIfscCode())
                .receiverName(request.getReceiverName())
                .receiverBankName(bankName)
                .amount(request.getAmount())
                .transactionType(transactionType)
                .transactionMode(TransactionMode.ONLINE)
                .paymentMethod(PaymentMethod.ACCOUNT_TRANSFER)
                .status(TransactionStatus.INITIATED)
                .category(category)
                .description(request.getDescription())
                .remarks(request.getRemarks())
                .utrNumber(idGenerator.generateUtrNumber())
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

        // Update limits
        TransactionLimit limit = limitService.getOrCreateLimit(customer);
        limitService.validateAndUpdateLimit(limit, transactionType, totalAmount);

        // Execute transaction (will simulate 30-second delay for external)
        executionService.executeTransaction(transaction);

        // Send notifications
        notificationService.sendTransactionNotification(transaction);

        log.info("External {} transfer completed: {} to {} ({})",
                transactionType, transaction.getTransactionId(),
                request.getReceiverAccountNumber(), bankName);

        return mapToTransactionResponse(transaction);
    }

    private TransactionType determineTransferType(String transferMode) {
        switch (transferMode.toUpperCase()) {
            case "IMPS":
                return TransactionType.IMPS;
            case "NEFT":
                return TransactionType.NEFT;
            case "RTGS":
                return TransactionType.RTGS;
            default:
                throw new InvalidRequestException("Invalid transfer mode: " + transferMode);
        }
    }

    private void validateTransferModeAndAmount(TransactionType type, BigDecimal amount) {
        switch (type) {
            case IMPS:
                if (amount.compareTo(BigDecimal.valueOf(200000)) > 0) {
                    throw new InvalidRequestException("IMPS transfer limit is ₹2,00,000");
                }
                break;
            case NEFT:
                if (amount.compareTo(BigDecimal.valueOf(10000000)) > 0) {
                    throw new InvalidRequestException("NEFT transfer limit is ₹1,00,00,000");
                }
                break;
            case RTGS:
                if (amount.compareTo(BigDecimal.valueOf(200000)) < 0) {
                    throw new InvalidRequestException("RTGS minimum amount is ₹2,00,000");
                }
                break;
        }
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
                .senderAccountNumber(transaction.getSenderAccount().getAccountNumber())
                .receiverAccountNumber(transaction.getReceiverAccountNumber())
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