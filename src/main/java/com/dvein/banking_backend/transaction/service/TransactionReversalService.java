package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionReversalService {

    private final TransactionRepository transactionRepository;
    private final TransactionExecutionService executionService;
    private final TransactionNotificationService notificationService;

    @Transactional
    public Transaction reverseTransaction(Long transactionId, String reason, String adminEmail) {
        Transaction originalTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        validateReversal(originalTransaction);

        // Create reversal transaction
        Transaction reversalTransaction = Transaction.builder()
                .transactionId("REV-" + originalTransaction.getTransactionId())
                .idempotencyKey("REV-" + originalTransaction.getIdempotencyKey())
                .senderAccount(originalTransaction.getReceiverAccount())
                .receiverAccount(originalTransaction.getSenderAccount())
                .receiverAccountNumber(originalTransaction.getSenderAccount() != null ?
                        originalTransaction.getSenderAccount().getAccountNumber() : null)
                .receiverIfscCode(originalTransaction.getSenderAccount() != null ?
                        originalTransaction.getSenderAccount().getIfscCode() : null)
                .receiverName(originalTransaction.getSenderAccount() != null ?
                        originalTransaction.getSenderAccount().getCustomer().getFullName() : null)
                .amount(originalTransaction.getAmount())
                .transactionType(originalTransaction.getTransactionType())
                .transactionMode(originalTransaction.getTransactionMode())
                .paymentMethod(originalTransaction.getPaymentMethod())
                .status(TransactionStatus.INITIATED)
                .category(originalTransaction.getCategory())
                .description("Reversal - " + (originalTransaction.getDescription() != null ? originalTransaction.getDescription() : ""))
                .reversalReason(reason)
                .transactionFee(BigDecimal.ZERO)
                .gst(BigDecimal.ZERO)
                .totalAmount(originalTransaction.getAmount())
                .ipAddress("SYSTEM")
                .build();

        reversalTransaction = transactionRepository.save(reversalTransaction);

        // Execute the reversal
        executionService.executeTransaction(reversalTransaction);

        // Update original transaction
        originalTransaction.setStatus(TransactionStatus.REVERSED);
        originalTransaction.setReversedAt(LocalDateTime.now());
        originalTransaction.setReversalTransaction(reversalTransaction);
        originalTransaction.setReversalReason(reason);
        transactionRepository.save(originalTransaction);

        // Send notifications
        notificationService.sendTransactionNotification(reversalTransaction);

        log.info("Transaction reversed: {} by admin: {} - Reason: {}", transactionId, adminEmail, reason);

        return reversalTransaction;
    }

    @Transactional
    public Transaction refundTransaction(Long transactionId, String reason, String adminEmail) {
        Transaction originalTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        validateRefund(originalTransaction);

        // Create refund transaction
        Transaction refundTransaction = Transaction.builder()
                .transactionId("RFND-" + originalTransaction.getTransactionId())
                .idempotencyKey("RFND-" + originalTransaction.getIdempotencyKey())
                .senderAccount(originalTransaction.getReceiverAccount())
                .receiverAccount(originalTransaction.getSenderAccount())
                .receiverAccountNumber(originalTransaction.getSenderAccount() != null ?
                        originalTransaction.getSenderAccount().getAccountNumber() : null)
                .receiverIfscCode(originalTransaction.getSenderAccount() != null ?
                        originalTransaction.getSenderAccount().getIfscCode() : null)
                .receiverName(originalTransaction.getSenderAccount() != null ?
                        originalTransaction.getSenderAccount().getCustomer().getFullName() : null)
                .amount(originalTransaction.getAmount())
                .transactionType(originalTransaction.getTransactionType())
                .transactionMode(originalTransaction.getTransactionMode())
                .paymentMethod(originalTransaction.getPaymentMethod())
                .status(TransactionStatus.INITIATED)
                .category(originalTransaction.getCategory())
                .description("Refund - " + (originalTransaction.getDescription() != null ? originalTransaction.getDescription() : ""))
                .reversalReason(reason)
                .transactionFee(BigDecimal.ZERO)
                .gst(BigDecimal.ZERO)
                .totalAmount(originalTransaction.getAmount())
                .ipAddress("SYSTEM")
                .build();

        refundTransaction = transactionRepository.save(refundTransaction);

        // Execute the refund
        executionService.executeTransaction(refundTransaction);

        // Update original transaction
        originalTransaction.setStatus(TransactionStatus.REFUNDED);
        originalTransaction.setReversalReason(reason);
        transactionRepository.save(originalTransaction);

        // Send notifications
        notificationService.sendTransactionNotification(refundTransaction);

        log.info("Transaction refunded: {} by admin: {} - Reason: {}", transactionId, adminEmail, reason);

        return refundTransaction;
    }

    @Transactional
    public Transaction partialRefundTransaction(Long transactionId, BigDecimal refundAmount, String reason, String adminEmail) {
        Transaction originalTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        if (refundAmount.compareTo(originalTransaction.getAmount()) > 0) {
            throw new InvalidRequestException("Refund amount cannot exceed original transaction amount");
        }

        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Refund amount must be greater than zero");
        }

        validateRefund(originalTransaction);

        Transaction partialRefund = Transaction.builder()
                .transactionId("PRFND-" + originalTransaction.getTransactionId())
                .idempotencyKey("PRFND-" + originalTransaction.getIdempotencyKey() + "-" + refundAmount)
                .senderAccount(originalTransaction.getReceiverAccount())
                .receiverAccount(originalTransaction.getSenderAccount())
                .amount(refundAmount)
                .transactionType(originalTransaction.getTransactionType())
                .transactionMode(originalTransaction.getTransactionMode())
                .paymentMethod(originalTransaction.getPaymentMethod())
                .status(TransactionStatus.INITIATED)
                .description("Partial Refund - " + refundAmount + " - " + (originalTransaction.getDescription() != null ? originalTransaction.getDescription() : ""))
                .reversalReason(reason)
                .transactionFee(BigDecimal.ZERO)
                .gst(BigDecimal.ZERO)
                .totalAmount(refundAmount)
                .ipAddress("SYSTEM")
                .build();

        partialRefund = transactionRepository.save(partialRefund);

        executionService.executeTransaction(partialRefund);

        log.info("Partial refund processed: {} for amount: {} by admin: {}", transactionId, refundAmount, adminEmail);

        return partialRefund;
    }

    private void validateReversal(Transaction transaction) {
        if (transaction.getStatus() != TransactionStatus.COMPLETED) {
            throw new InvalidRequestException("Only completed transactions can be reversed. Current status: " + transaction.getStatus());
        }

        if (transaction.getReversalTransaction() != null) {
            throw new InvalidRequestException("Transaction has already been reversed");
        }

        if (transaction.getReceiverAccount() == null && transaction.getSenderAccount() == null) {
            throw new InvalidRequestException("Cannot reverse external transaction automatically. Manual reversal required.");
        }
    }

    private void validateRefund(Transaction transaction) {
        if (transaction.getStatus() != TransactionStatus.COMPLETED) {
            throw new InvalidRequestException("Only completed transactions can be refunded. Current status: " + transaction.getStatus());
        }
    }
}