package com.dvein.banking_backend.transaction.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TransactionIdGenerator {

    private static final String PREFIX = "TXN";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final AtomicLong counter = new AtomicLong(0);

    public String generateTransactionId() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        long count = counter.incrementAndGet() % 10000;
        return String.format("%s%s%04d", PREFIX, timestamp, count);
    }

    public String generateReceiptNumber() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        long count = counter.incrementAndGet() % 10000;
        return String.format("REC%s%04d", timestamp, count);
    }

    public String generateUtrNumber() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        long count = counter.incrementAndGet() % 10000;
        return String.format("UTR%s%04d", timestamp, count);
    }

    public String generateRequestId() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        long count = counter.incrementAndGet() % 10000;
        return String.format("REQ%s%04d", timestamp, count);
    }

    public String generateQrId() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        long count = counter.incrementAndGet() % 10000;
        return String.format("QR%s%04d", timestamp, count);
    }
}