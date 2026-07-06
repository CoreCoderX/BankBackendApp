package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.transaction.dto.response.TransactionReceiptResponse;
import com.dvein.banking_backend.transaction.dto.response.TransactionResponse;
import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.model.TransactionReceipt;
import com.dvein.banking_backend.transaction.repository.TransactionReceiptRepository;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import com.dvein.banking_backend.transaction.util.TransactionIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionReceiptService {

    private final TransactionReceiptRepository receiptRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionIdGenerator idGenerator;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    @Transactional
    public TransactionReceiptResponse generateReceipt(Long transactionId, String email) {
        Transaction transaction = transactionRepository.findByIdAndUserEmail(transactionId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        // Check if receipt already exists
        TransactionReceipt existingReceipt = receiptRepository.findByTransaction(transaction).orElse(null);
        if (existingReceipt != null) {
            return mapToReceiptResponse(existingReceipt, transaction);
        }

        // Generate new receipt
        String receiptNumber = idGenerator.generateReceiptNumber();
        String receiptData = buildReceiptData(transaction);
        String qrCode = generateReceiptQrCode(transaction);

        TransactionReceipt receipt = TransactionReceipt.builder()
                .transaction(transaction)
                .receiptNumber(receiptNumber)
                .receiptData(receiptData)
                .qrCode(qrCode)
                .build();

        receipt = receiptRepository.save(receipt);
        log.info("Receipt generated for transaction: {}", transaction.getTransactionId());

        return mapToReceiptResponse(receipt, transaction);
    }

    public TransactionReceiptResponse getReceipt(Long transactionId, String email) {
        Transaction transaction = transactionRepository.findByIdAndUserEmail(transactionId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        TransactionReceipt receipt = receiptRepository.findByTransaction(transaction)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found for this transaction"));

        return mapToReceiptResponse(receipt, transaction);
    }

    private String buildReceiptData(Transaction transaction) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("============================================\n");
        receipt.append("         TRANSACTION RECEIPT\n");
        receipt.append("           DVein Bank Ltd.\n");
        receipt.append("============================================\n\n");

        receipt.append("Transaction ID: ").append(transaction.getTransactionId()).append("\n");
        receipt.append("Date: ").append(transaction.getCompletedAt().format(DATE_FORMATTER)).append("\n");
        receipt.append("Status: ").append(transaction.getStatus()).append("\n\n");

        receipt.append("--------------------------------------------\n");
        receipt.append("TRANSACTION DETAILS\n");
        receipt.append("--------------------------------------------\n");
        receipt.append("Type: ").append(transaction.getTransactionType()).append("\n");
        receipt.append("Mode: ").append(transaction.getPaymentMethod()).append("\n");
        receipt.append("Amount: Rs. ").append(String.format("%.2f", transaction.getAmount())).append("\n");

        if (transaction.getTransactionFee().compareTo(java.math.BigDecimal.ZERO) > 0) {
            receipt.append("Fee: Rs. ").append(String.format("%.2f", transaction.getTransactionFee())).append("\n");
            receipt.append("GST: Rs. ").append(String.format("%.2f", transaction.getGst())).append("\n");
            receipt.append("Total: Rs. ").append(String.format("%.2f", transaction.getTotalAmount())).append("\n");
        }

        receipt.append("\n--------------------------------------------\n");
        receipt.append("FROM\n");
        receipt.append("--------------------------------------------\n");
        if (transaction.getSenderAccount() != null) {
            receipt.append("Account: ").append(transaction.getSenderAccount().getAccountNumber()).append("\n");
            receipt.append("Name: ").append(transaction.getSenderAccount().getCustomer().getFullName()).append("\n");
        }

        receipt.append("\n--------------------------------------------\n");
        receipt.append("TO\n");
        receipt.append("--------------------------------------------\n");
        if (transaction.getReceiverAccount() != null) {
            receipt.append("Account: ").append(transaction.getReceiverAccount().getAccountNumber()).append("\n");
            receipt.append("Name: ").append(transaction.getReceiverAccount().getCustomer().getFullName()).append("\n");
        } else {
            receipt.append("Account: ").append(transaction.getReceiverAccountNumber()).append("\n");
            receipt.append("Name: ").append(transaction.getReceiverName()).append("\n");
            receipt.append("Bank: ").append(transaction.getReceiverBankName()).append("\n");
        }

        if (transaction.getUtrNumber() != null) {
            receipt.append("\nUTR Number: ").append(transaction.getUtrNumber()).append("\n");
        }

        if (transaction.getDescription() != null) {
            receipt.append("\nDescription: ").append(transaction.getDescription()).append("\n");
        }

        receipt.append("\n============================================\n");
        receipt.append("This is a computer-generated receipt\n");
        receipt.append("============================================\n");

        return receipt.toString();
    }

    private String generateReceiptQrCode(Transaction transaction) {
        // Simulate QR code generation
        String qrData = "TXN:" + transaction.getTransactionId() +
                "|AMT:" + transaction.getAmount() +
                "|DATE:" + transaction.getCompletedAt();
        return Base64.getEncoder().encodeToString(qrData.getBytes());
    }

    private TransactionReceiptResponse mapToReceiptResponse(TransactionReceipt receipt, Transaction transaction) {
        return TransactionReceiptResponse.builder()
                .receiptNumber(receipt.getReceiptNumber())
                .receiptData(receipt.getReceiptData())
                .qrCode(receipt.getQrCode())
                .generatedAt(receipt.getGeneratedAt())
                .transaction(mapToTransactionResponse(transaction))
                .build();
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .initiatedAt(transaction.getInitiatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }
}