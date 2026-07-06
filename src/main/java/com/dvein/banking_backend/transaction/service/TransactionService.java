package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.account.repository.CustomerRepository;
import com.dvein.banking_backend.common.dto.PageResponse;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.transaction.dto.request.TransactionSearchRequest;
import com.dvein.banking_backend.transaction.dto.response.*;
import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.enums.TransactionType;
import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public TransactionResponse getTransactionById(Long transactionId, String email) {
        Transaction transaction = transactionRepository.findByIdAndUserEmail(transactionId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        return mapToTransactionResponse(transaction);
    }

    public TransactionListResponse getMyTransactions(Long accountId, String email, int page, int size) {
        Account account = accountRepository.findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        Pageable pageable = PageRequest.of(page, size, Sort.by("initiatedAt").descending());

        Page<Transaction> transactionPage = transactionRepository
                .findBySenderAccountOrReceiverAccountOrderByInitiatedAtDesc(account, account, pageable);

        List<TransactionResponse> transactions = transactionPage.getContent()
                .stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());

        PageResponse<TransactionResponse> pageResponse = PageResponse.<TransactionResponse>builder()
                .content(transactions)
                .pageNumber(transactionPage.getNumber())
                .pageSize(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .last(transactionPage.isLast())
                .first(transactionPage.isFirst())
                .build();

        // Get counts
        List<Transaction> allTransactions = transactionRepository
                .findBySenderAccountOrReceiverAccountOrderByInitiatedAtDesc(account, account);

        long completedCount = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .count();

        long failedCount = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.FAILED)
                .count();

        long pendingCount = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.PENDING ||
                        t.getStatus() == TransactionStatus.PROCESSING)
                .count();

        return TransactionListResponse.builder()
                .transactions(pageResponse)
                .totalCount(transactionPage.getTotalElements())
                .completedCount(completedCount)
                .failedCount(failedCount)
                .pendingCount(pendingCount)
                .build();
    }

    public List<TransactionResponse> getMiniStatement(Long accountId, String email) {
        Account account = accountRepository.findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        // Get last 10 transactions
        Pageable pageable = PageRequest.of(0, 10, Sort.by("initiatedAt").descending());

        Page<Transaction> transactionPage = transactionRepository
                .findBySenderAccountOrReceiverAccountOrderByInitiatedAtDesc(account, account, pageable);

        return transactionPage.getContent()
                .stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    public TransactionSummaryResponse getTransactionSummary(Long accountId, String email) {
        Account account = accountRepository.findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        List<Transaction> allTransactions = transactionRepository
                .findBySenderAccountOrReceiverAccountOrderByInitiatedAtDesc(account, account);

        long totalTransactions = allTransactions.size();
        long completedCount = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .count();
        long failedCount = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.FAILED)
                .count();
        long pendingCount = allTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.PENDING ||
                        t.getStatus() == TransactionStatus.PROCESSING)
                .count();

        BigDecimal totalSent = allTransactions.stream()
                .filter(t -> t.getSenderAccount() != null &&
                        t.getSenderAccount().getId().equals(accountId) &&
                        t.getStatus() == TransactionStatus.COMPLETED)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReceived = allTransactions.stream()
                .filter(t -> t.getReceiverAccount() != null &&
                        t.getReceiverAccount().getId().equals(accountId) &&
                        t.getStatus() == TransactionStatus.COMPLETED)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFees = allTransactions.stream()
                .filter(t -> t.getSenderAccount() != null &&
                        t.getSenderAccount().getId().equals(accountId) &&
                        t.getStatus() == TransactionStatus.COMPLETED)
                .map(t -> t.getTransactionFee().add(t.getGst()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return TransactionSummaryResponse.builder()
                .totalTransactions(totalTransactions)
                .completedTransactions(completedCount)
                .failedTransactions(failedCount)
                .pendingTransactions(pendingCount)
                .totalSentAmount(totalSent)
                .totalReceivedAmount(totalReceived)
                .totalFeesPaid(totalFees)
                .currentBalance(account.getBalance())
                .build();
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