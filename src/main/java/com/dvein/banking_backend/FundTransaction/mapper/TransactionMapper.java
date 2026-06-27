package com.dvein.banking_backend.FundTransaction.mapper;

import com.dvein.banking_backend.FundTransaction.dto.response.TransactionDetailResponse;
import com.dvein.banking_backend.FundTransaction.dto.response.TransactionHistoryResponse;
import com.dvein.banking_backend.FundTransaction.dto.response.TransactionResponse;
import com.dvein.banking_backend.FundTransaction.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .referenceNumber(transaction.getReferenceNumber())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .remarks(transaction.getRemarks())
                .senderAccountNumber(
                        transaction.getSenderAccount() != null ?
                                transaction.getSenderAccount().getAccountNumber() : null
                )
                .receiverAccountNumber(
                        transaction.getReceiverAccount() != null ?
                                transaction.getReceiverAccount().getAccountNumber() : null
                )
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    public TransactionDetailResponse toDetailResponse(Transaction transaction) {
        return TransactionDetailResponse.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .referenceNumber(transaction.getReferenceNumber())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .channel(transaction.getChannel())
                .remarks(transaction.getRemarks())
                .senderAccountNumber(
                        transaction.getSenderAccount() != null ?
                                transaction.getSenderAccount().getAccountNumber() : null
                )
                .senderAccountHolderName(
                        transaction.getSenderAccount() != null &&
                                transaction.getSenderAccount().getCustomer() != null ?
                                transaction.getSenderAccount().getCustomer().getFullName() : null
                )
                .receiverAccountNumber(
                        transaction.getReceiverAccount() != null ?
                                transaction.getReceiverAccount().getAccountNumber() : null
                )
                .receiverAccountHolderName(
                        transaction.getReceiverAccount() != null &&
                                transaction.getReceiverAccount().getCustomer() != null ?
                                transaction.getReceiverAccount().getCustomer().getFullName() : null
                )
                .ipAddress(transaction.getIpAddress())
                .deviceInfo(transaction.getDeviceInfo())
                .isFlagged(transaction.getIsFlagged())
                .flagReason(transaction.getFlagReason())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }

    public TransactionHistoryResponse toHistoryResponse(Transaction transaction) {
        String description = buildDescription(transaction);

        return TransactionHistoryResponse.builder()
                .transactionId(transaction.getTransactionId())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .description(description)
                .transactionDate(transaction.getCreatedAt())
                .build();
    }

    private String buildDescription(Transaction transaction) {
        String type = transaction.getTransactionType().name().replace("_", " ");
        String sender = transaction.getSenderAccount() != null ?
                maskAccountNumber(transaction.getSenderAccount().getAccountNumber()) : "N/A";
        String receiver = transaction.getReceiverAccount() != null ?
                maskAccountNumber(transaction.getReceiverAccount().getAccountNumber()) : "N/A";

        return String.format("%s from %s to %s", type, sender, receiver);
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        int length = accountNumber.length();
        return "XXXX" + accountNumber.substring(length - 4);
    }
}