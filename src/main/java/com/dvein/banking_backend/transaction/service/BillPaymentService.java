package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.repository.CustomerRepository;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.transaction.dto.request.BillPaymentRequest;
import com.dvein.banking_backend.transaction.dto.request.SaveBillerRequest;
import com.dvein.banking_backend.transaction.dto.response.BillPaymentResponse;
import com.dvein.banking_backend.transaction.dto.response.BillerResponse;
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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillPaymentService {

    private final TransactionValidationService validationService;
    private final TransactionExecutionService executionService;
    private final TransactionFeeService feeService;
    private final TransactionLimitService limitService;
    private final FraudDetectionService fraudDetectionService;
    private final TransactionNotificationService notificationService;
    private final TransactionRepository transactionRepository;
    private final BillPaymentRepository billPaymentRepository;
    private final BillerRepository billerRepository;
    private final CustomerRepository customerRepository;
    private final TransactionCategoryRepository categoryRepository;
    private final TransactionIdGenerator idGenerator;

    @Transactional
    public BillPaymentResponse payBill(BillPaymentRequest request, String email, HttpServletRequest httpRequest) {
        // Validate idempotency
        validationService.validateIdempotency(request.getIdempotencyKey());

        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        // Get sender account
        Account senderAccount = validationService.getAndValidateSenderAccount(request.getAccountId(), email);
        validationService.validateCustomerStatus(senderAccount.getCustomer());
        validationService.validateKycStatus(senderAccount.getCustomer());

        // Calculate total amount
        BigDecimal totalBillAmount = request.getAmount();
        if (request.getLateFee() != null) {
            totalBillAmount = totalBillAmount.add(request.getLateFee());
        }

        // Calculate fees (bill payment is free)
        TransactionFeeService.FeeCalculation feeCalc = feeService.calculateFee(
                TransactionType.BILL_PAYMENT, totalBillAmount);
        BigDecimal totalAmount = totalBillAmount.add(feeCalc.getTotalFee());

        // Validate balance
        validationService.validateSufficientBalance(senderAccount, totalAmount);

        // Get category
        TransactionCategory category = categoryRepository.findByName("BILL_PAYMENT").orElse(null);

        // Create transaction
        Transaction transaction = Transaction.builder()
                .transactionId(idGenerator.generateTransactionId())
                .idempotencyKey(request.getIdempotencyKey())
                .senderAccount(senderAccount)
                .receiverAccount(null)
                .receiverName(request.getBillerName())
                .amount(totalBillAmount)
                .transactionType(TransactionType.BILL_PAYMENT)
                .transactionMode(TransactionMode.ONLINE)
                .paymentMethod(PaymentMethod.ACCOUNT_TRANSFER)
                .status(TransactionStatus.INITIATED)
                .category(category)
                .description("Bill Payment - " + request.getBillCategory())
                .transactionFee(feeCalc.getBaseFee())
                .gst(feeCalc.getGst())
                .totalAmount(totalAmount)
                .ipAddress(getClientIp(httpRequest))
                .deviceId(httpRequest.getHeader("X-Device-ID"))
                .build();

        transaction = transactionRepository.save(transaction);

        // Fraud detection
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
        limitService.validateAndUpdateLimit(limit, TransactionType.BILL_PAYMENT, totalAmount);

        // Save biller if requested
        Biller biller = null;
        if (request.getBillerId() != null) {
            biller = billerRepository.findById(request.getBillerId())
                    .orElse(null);
        } else if (request.getSaveBiller() != null && request.getSaveBiller()) {
            biller = saveBiller(customer, request);
        }

        // Create bill payment record
        BillPayment billPayment = BillPayment.builder()
                .transaction(transaction)
                .biller(biller)
                .billCategory(request.getBillCategory())
                .billNumber(request.getBillNumber())
                .lateFee(request.getLateFee() != null ? request.getLateFee() : BigDecimal.ZERO)
                .build();

        billPaymentRepository.save(billPayment);

        // Execute transaction
        executionService.executeTransaction(transaction);

        // Send notifications
        notificationService.sendTransactionNotification(transaction);

        log.info("Bill payment completed: {} for category: {}",
                transaction.getTransactionId(), request.getBillCategory());

        return BillPaymentResponse.builder()
                .transaction(mapToTransactionResponse(transaction))
                .billerName(request.getBillerName())
                .billCategory(request.getBillCategory())
                .billNumber(request.getBillNumber())
                .lateFee(request.getLateFee())
                .build();
    }

    @Transactional
    public BillerResponse addBiller(SaveBillerRequest request, String email) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        Biller biller = Biller.builder()
                .customer(customer)
                .billerName(request.getBillerName())
                .billerCategory(request.getBillerCategory())
                .accountNumber(request.getAccountNumber())
                .nickname(request.getNickname())
                .autoPayEnabled(request.getAutoPayEnabled() != null ? request.getAutoPayEnabled() : false)
                .build();

        biller = billerRepository.save(biller);
        log.info("Biller saved for customer: {}", customer.getId());

        return mapToBillerResponse(biller);
    }

    public List<BillerResponse> getMyBillers(String email) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        List<Biller> billers = billerRepository.findByCustomerOrderByCreatedAtDesc(customer);

        return billers.stream()
                .map(this::mapToBillerResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteBiller(Long billerId, String email) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        Biller biller = billerRepository.findById(billerId)
                .orElseThrow(() -> new ResourceNotFoundException("Biller", "id", billerId));

        if (!biller.getCustomer().getId().equals(customer.getId())) {
            throw new ResourceNotFoundException("Biller not found");
        }

        billerRepository.delete(biller);
        log.info("Biller deleted: {}", billerId);
    }

    private Biller saveBiller(Customer customer, BillPaymentRequest request) {
        Biller biller = Biller.builder()
                .customer(customer)
                .billerName(request.getBillerName())
                .billerCategory(request.getBillCategory())
                .accountNumber(request.getBillAccountNumber())
                .nickname(request.getBillerNickname())
                .autoPayEnabled(false)
                .build();

        return billerRepository.save(biller);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private BillerResponse mapToBillerResponse(Biller biller) {
        return BillerResponse.builder()
                .id(biller.getId())
                .billerName(biller.getBillerName())
                .billerCategory(biller.getBillerCategory())
                .accountNumber(biller.getAccountNumber())
                .nickname(biller.getNickname())
                .autoPayEnabled(biller.isAutoPayEnabled())
                .createdAt(biller.getCreatedAt())
                .build();
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .senderAccountNumber(transaction.getSenderAccount().getAccountNumber())
                .receiverName(transaction.getReceiverName())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
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