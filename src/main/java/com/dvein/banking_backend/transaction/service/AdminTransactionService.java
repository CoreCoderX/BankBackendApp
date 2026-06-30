package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.common.dto.PageResponse;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.transaction.dto.response.TransactionResponse;
import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminTransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionExecutionService executionService;

    public PageResponse<TransactionResponse> getAllTransactions(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Transaction> transactionPage = transactionRepository.findAll(pageable);

        List<TransactionResponse> transactions = transactionPage.getContent()
                .stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());

        return PageResponse.<TransactionResponse>builder()
                .content(transactions)
                .pageNumber(transactionPage.getNumber())
                .pageSize(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .last(transactionPage.isLast())
                .first(transactionPage.isFirst())
                .build();
    }

    public PageResponse<TransactionResponse> getPendingApprovals(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("initiatedAt").descending());

        Page<Transaction> transactionPage = transactionRepository.findPendingApprovals(pageable);

        List<TransactionResponse> transactions = transactionPage.getContent()
                .stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());

        return PageResponse.<TransactionResponse>builder()
                .content(transactions)
                .pageNumber(transactionPage.getNumber())
                .pageSize(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .last(transactionPage.isLast())
                .first(transactionPage.isFirst())
                .build();
    }

    public List<TransactionResponse> getFailedTransactions() {
        List<Transaction> failedTransactions = transactionRepository
                .findByStatusAndInitiatedAtBefore(TransactionStatus.FAILED, java.time.LocalDateTime.now());

        return failedTransactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void reverseTransaction(Long transactionId, String reason, String adminEmail) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        if (transaction.getStatus() != TransactionStatus.COMPLETED) {
            throw new InvalidRequestException("Only completed transactions can be reversed");
        }

        if (transaction.getReversalTransaction() != null) {
            throw new InvalidRequestException("Transaction already reversed");
        }

        // Create reversal transaction
        Transaction reversalTxn = Transaction.builder()
                .transactionId("REV-" + transaction.getTransactionId())
                .senderAccount(transaction.getReceiverAccount())
                .receiverAccount(transaction.getSenderAccount())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .transactionMode(transaction.getTransactionMode())
                .paymentMethod(transaction.getPaymentMethod())
                .status(TransactionStatus.INITIATED)
                .description("Reversal - " + transaction.getDescription())
                .reversalReason(reason)
                .transactionFee(java.math.BigDecimal.ZERO)
                .gst(java.math.BigDecimal.ZERO)
                .totalAmount(transaction.getAmount())
                .build();

        reversalTxn = transactionRepository.save(reversalTxn);

        // Execute reversal
        executionService.executeTransaction(reversalTxn);

        // Update original transaction
        transaction.setStatus(TransactionStatus.REVERSED);
        transaction.setReversedAt(java.time.LocalDateTime.now());
        transaction.setReversalTransaction(reversalTxn);
        transaction.setReversalReason(reason);
        transactionRepository.save(transaction);

        log.info("Transaction reversed: {} by admin: {}", transactionId, adminEmail);
    }

    @Transactional
    public void refundTransaction(Long transactionId, String reason, String adminEmail) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        if (transaction.getStatus() != TransactionStatus.COMPLETED) {
            throw new InvalidRequestException("Only completed transactions can be refunded");
        }

        // Similar to reversal but marks as REFUNDED
        Transaction refundTxn = Transaction.builder()
                .transactionId("RFND-" + transaction.getTransactionId())
                .senderAccount(transaction.getReceiverAccount())
                .receiverAccount(transaction.getSenderAccount())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .transactionMode(transaction.getTransactionMode())
                .paymentMethod(transaction.getPaymentMethod())
                .status(TransactionStatus.INITIATED)
                .description("Refund - " + transaction.getDescription())
                .transactionFee(java.math.BigDecimal.ZERO)
                .gst(java.math.BigDecimal.ZERO)
                .totalAmount(transaction.getAmount())
                .build();

        refundTxn = transactionRepository.save(refundTxn);

        executionService.executeTransaction(refundTxn);

        transaction.setStatus(TransactionStatus.REFUNDED);
        transaction.setReversalReason(reason);
        transactionRepository.save(transaction);

        log.info("Transaction refunded: {} by admin: {}", transactionId, adminEmail);
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