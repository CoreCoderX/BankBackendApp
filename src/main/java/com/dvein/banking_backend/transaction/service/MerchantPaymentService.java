package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.transaction.dto.request.MerchantPaymentRequest;
import com.dvein.banking_backend.transaction.dto.response.TransactionResponse;
import com.dvein.banking_backend.transaction.enums.*;
import com.dvein.banking_backend.transaction.exception.FraudDetectedException;
import com.dvein.banking_backend.transaction.model.*;
import com.dvein.banking_backend.transaction.repository.*;
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
public class MerchantPaymentService {

    private final TransactionValidationService validationService;
    private final TransactionExecutionService executionService;
    private final TransactionFeeService feeService;
    private final TransactionLimitService limitService;
    private final FraudDetectionService fraudDetectionService;
    private final TransactionNotificationService notificationService;
    private final TransactionRepository transactionRepository;
    private final MerchantPaymentRepository merchantPaymentRepository;
    private final MerchantRepository merchantRepository;
    private final TransactionCategoryRepository categoryRepository;
    private final TransactionIdGenerator idGenerator;

    @Transactional
    public TransactionResponse payMerchant(MerchantPaymentRequest request, String email, HttpServletRequest httpRequest) {
        // Validate idempotency
        validationService.validateIdempotency(request.getIdempotencyKey());

        // Get merchant
        Merchant merchant = merchantRepository.findByMerchantCode(request.getMerchantCode())
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", "merchantCode", request.getMerchantCode()));

        if (!merchant.isActive()) {
            throw new InvalidRequestException("Merchant is not active");
        }

        // Get sender account
        Account senderAccount = validationService.getAndValidateSenderAccount(request.getAccountId(), email);
        validationService.validateCustomerStatus(senderAccount.getCustomer());
        validationService.validateKycStatus(senderAccount.getCustomer());

        // Calculate fees (merchant payment is free)
        TransactionFeeService.FeeCalculation feeCalc = feeService.calculateFee(
                TransactionType.MERCHANT_PAYMENT, request.getAmount());
        BigDecimal totalAmount = request.getAmount().add(feeCalc.getTotalFee());

        // Validate balance
        validationService.validateSufficientBalance(senderAccount, totalAmount);

        // Determine payment method
        PaymentMethod paymentMethod = PaymentMethod.valueOf(request.getPaymentMethod());

        // Get category
        TransactionCategory category = categoryRepository.findByName("MERCHANT").orElse(null);

        // Create transaction
        Transaction transaction = Transaction.builder()
                .transactionId(idGenerator.generateTransactionId())
                .idempotencyKey(request.getIdempotencyKey())
                .senderAccount(senderAccount)
                .receiverAccount(null)
                .receiverName(merchant.getMerchantName())
                .amount(request.getAmount())
                .transactionType(TransactionType.MERCHANT_PAYMENT)
                .transactionMode(TransactionMode.ONLINE)
                .paymentMethod(paymentMethod)
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
            transaction.setFailureReason("Transaction blocked");
            transaction.setFlagged(true);
            transactionRepository.save(transaction);

            notificationService.sendFraudAlert(email, transaction.getTransactionId(), fraudCheck.getDetails());
            throw new FraudDetectedException("Transaction blocked");
        }

        // Update limits
        TransactionLimit limit = limitService.getOrCreateLimit(customer);
        limitService.validateAndUpdateLimit(limit, TransactionType.MERCHANT_PAYMENT, totalAmount);

        // Calculate cashback and rewards (simple logic)
        BigDecimal cashbackAmount = calculateCashback(merchant, request.getAmount());
        int rewardPoints = calculateRewardPoints(request.getAmount());

        // Create merchant payment record
        MerchantPayment merchantPayment = MerchantPayment.builder()
                .transaction(transaction)
                .merchant(merchant)
                .merchantReferenceId(request.getMerchantReferenceId())
                .cashbackAmount(cashbackAmount)
                .rewardPoints(rewardPoints)
                .build();

        merchantPaymentRepository.save(merchantPayment);

        // Execute transaction
        executionService.executeTransaction(transaction);

        // Send notifications
        notificationService.sendTransactionNotification(transaction);

        log.info("Merchant payment completed: {} to merchant: {}",
                transaction.getTransactionId(), merchant.getMerchantCode());

        return mapToTransactionResponse(transaction);
    }

    private BigDecimal calculateCashback(Merchant merchant, BigDecimal amount) {
        // Simple cashback logic: 1% for verified merchants
        if (merchant.isVerified()) {
            return amount.multiply(BigDecimal.valueOf(0.01))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private int calculateRewardPoints(BigDecimal amount) {
        // Simple reward logic: 1 point per ₹100
        return amount.divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.DOWN).intValue();
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
                .receiverName(transaction.getReceiverName())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .paymentMethod(transaction.getPaymentMethod())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .transactionFee(transaction.getTransactionFee())
                .gst(transaction.getGst())
                .totalAmount(transaction.getTotalAmount())
                .initiatedAt(transaction.getInitiatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }
}