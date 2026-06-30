package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.transaction.dto.response.TransactionResponse;
import com.dvein.banking_backend.transaction.dto.response.TransactionStatementResponse;
import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import com.dvein.banking_backend.transaction.util.TransactionIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionStatementService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionReceiptService receiptService;
    private final TransactionIdGenerator idGenerator;

    public TransactionStatementResponse getStatement(Long accountId, LocalDate startDate, LocalDate endDate, String email) {
        Account account = accountRepository.findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Transaction> transactions = transactionRepository
                .findByAccountAndDateRange(account, startDateTime, endDateTime);

        List<TransactionResponse> transactionResponses = transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());

        // Calculate totals
        BigDecimal totalCredits = BigDecimal.ZERO;
        BigDecimal totalDebits = BigDecimal.ZERO;

        for (Transaction txn : transactions) {
            if (txn.getStatus() == TransactionStatus.COMPLETED) {
                if (txn.getReceiverAccount() != null && txn.getReceiverAccount().getId().equals(accountId)) {
                    totalCredits = totalCredits.add(txn.getAmount());
                }
                if (txn.getSenderAccount() != null && txn.getSenderAccount().getId().equals(accountId)) {
                    totalDebits = totalDebits.add(txn.getAmount());
                }
            }
        }

        // Calculate opening balance (current balance - net change)
        BigDecimal netChange = totalCredits.subtract(totalDebits);
        BigDecimal openingBalance = account.getBalance().subtract(netChange);

        return TransactionStatementResponse.builder()
                .accountNumber(account.getAccountNumber())
                .accountHolderName(account.getCustomer().getFullName())
                .startDate(startDate)
                .endDate(endDate)
                .openingBalance(openingBalance)
                .closingBalance(account.getBalance())
                .totalCredits(totalCredits)
                .totalDebits(totalDebits)
                .transactionCount(transactions.size())
                .transactions(transactionResponses)
                .generatedAt(LocalDate.now())
                .build();
    }

    @Transactional
    public String downloadStatement(Long accountId, LocalDate startDate, LocalDate endDate, String email) {
        TransactionStatementResponse statement = getStatement(accountId, startDate, endDate, email);

        StringBuilder csv = new StringBuilder();
        csv.append("Transaction Statement\n");
        csv.append("Account: ").append(statement.getAccountNumber()).append("\n");
        csv.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n");
        csv.append("Opening Balance: Rs. ").append(statement.getOpeningBalance()).append("\n");
        csv.append("Closing Balance: Rs. ").append(statement.getClosingBalance()).append("\n\n");
        csv.append("Date,Txn ID,Type,Amount,Status,Description\n");

        for (TransactionResponse txn : statement.getTransactions()) {
            csv.append(txn.getInitiatedAt() != null ? txn.getInitiatedAt().toLocalDate() : "")
                    .append(",").append(txn.getTransactionId())
                    .append(",").append(txn.getTransactionType())
                    .append(",").append(txn.getAmount())
                    .append(",").append(txn.getStatus())
                    .append(",").append(txn.getDescription() != null ? txn.getDescription().replace(",", ";") : "")
                    .append("\n");
        }

        return csv.toString();
    }

    @Transactional
    public void emailStatement(Long accountId, LocalDate startDate, LocalDate endDate, String email) {
        // The statement will be emailed asynchronously
        log.info("Statement email requested for account: {} period: {} to {}", accountId, startDate, endDate);
        // EmailService will be called with the statement data
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