package com.dvein.banking_backend.notification.template;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TransactionEmailTemplates {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    public static String buildTransactionSuccessEmail(
            String customerName,
            String transactionId,
            BigDecimal amount,
            String receiverName,
            LocalDateTime timestamp,
            BigDecimal balance) {

        return String.format(
                "Dear %s,\n\n" +
                        "Your transaction was successful.\n\n" +
                        "Transaction Details:\n" +
                        "Transaction ID: %s\n" +
                        "Amount: Rs. %.2f\n" +
                        "To: %s\n" +
                        "Date: %s\n" +
                        "Available Balance: Rs. %.2f\n\n" +
                        "If you did not authorize this transaction, please contact us immediately.\n\n" +
                        "Thank you for banking with DVein Bank.\n\n" +
                        "Best regards,\n" +
                        "DVein Bank Team",
                customerName,
                transactionId,
                amount,
                receiverName,
                timestamp.format(DATE_FORMATTER),
                balance
        );
    }

    public static String buildUpiTransactionEmail(
            String customerName,
            String transactionId,
            BigDecimal amount,
            String receiverUpiId,
            LocalDateTime timestamp) {

        return String.format(
                "Dear %s,\n\n" +
                        "Your UPI payment was successful.\n\n" +
                        "Transaction Details:\n" +
                        "Transaction ID: %s\n" +
                        "Amount: Rs. %.2f\n" +
                        "Paid to: %s\n" +
                        "Date: %s\n\n" +
                        "Thank you for using DVein Bank UPI.\n\n" +
                        "Best regards,\n" +
                        "DVein Bank Team",
                customerName,
                transactionId,
                amount,
                receiverUpiId,
                timestamp.format(DATE_FORMATTER)
        );
    }

    public static String buildBillPaymentEmail(
            String customerName,
            String transactionId,
            String billerName,
            String billCategory,
            BigDecimal amount,
            LocalDateTime timestamp) {

        return String.format(
                "Dear %s,\n\n" +
                        "Your bill payment was successful.\n\n" +
                        "Payment Details:\n" +
                        "Transaction ID: %s\n" +
                        "Biller: %s\n" +
                        "Category: %s\n" +
                        "Amount: Rs. %.2f\n" +
                        "Date: %s\n\n" +
                        "Thank you for using DVein Bank Bill Payment.\n\n" +
                        "Best regards,\n" +
                        "DVein Bank Team",
                customerName,
                transactionId,
                billerName,
                billCategory,
                amount,
                timestamp.format(DATE_FORMATTER)
        );
    }

    public static String buildScheduledPaymentExecutionEmail(
            String customerName,
            BigDecimal amount,
            String receiverName,
            LocalDateTime timestamp) {

        return String.format(
                "Dear %s,\n\n" +
                        "Your scheduled payment has been executed.\n\n" +
                        "Payment Details:\n" +
                        "Amount: Rs. %.2f\n" +
                        "Paid to: %s\n" +
                        "Date: %s\n\n" +
                        "This is an automated scheduled payment.\n\n" +
                        "Best regards,\n" +
                        "DVein Bank Team",
                customerName,
                amount,
                receiverName,
                timestamp.format(DATE_FORMATTER)
        );
    }

    public static String buildTransactionFailureEmail(
            String customerName,
            String transactionId,
            BigDecimal amount,
            String reason,
            LocalDateTime timestamp) {

        return String.format(
                "Dear %s,\n\n" +
                        "Your transaction has failed.\n\n" +
                        "Transaction Details:\n" +
                        "Transaction ID: %s\n" +
                        "Amount: Rs. %.2f\n" +
                        "Reason: %s\n" +
                        "Date: %s\n\n" +
                        "Please try again or contact customer support if the issue persists.\n\n" +
                        "Best regards,\n" +
                        "DVein Bank Team",
                customerName,
                transactionId,
                amount,
                reason,
                timestamp.format(DATE_FORMATTER)
        );
    }

    public static String buildLargeTransactionAlertEmail(
            String customerName,
            String transactionId,
            BigDecimal amount,
            String receiverName) {

        return String.format(
                "Dear %s,\n\n" +
                        "ALERT: Large Transaction Detected\n\n" +
                        "A large transaction was made from your account:\n\n" +
                        "Transaction ID: %s\n" +
                        "Amount: Rs. %.2f\n" +
                        "To: %s\n\n" +
                        "If you did not authorize this transaction, please contact us immediately.\n\n" +
                        "Security Team,\n" +
                        "DVein Bank",
                customerName,
                transactionId,
                amount,
                receiverName
        );
    }

    public static String buildMonthlyStatementEmail(
            String customerName,
            String accountNumber,
            String month,
            int transactionCount,
            BigDecimal totalDebits,
            BigDecimal totalCredits,
            BigDecimal closingBalance) {

        return String.format(
                "Dear %s,\n\n" +
                        "Your monthly account statement for %s is ready.\n\n" +
                        "Account: %s\n" +
                        "Total Transactions: %d\n" +
                        "Total Debits: Rs. %.2f\n" +
                        "Total Credits: Rs. %.2f\n" +
                        "Closing Balance: Rs. %.2f\n\n" +
                        "Please login to download your detailed statement.\n\n" +
                        "Best regards,\n" +
                        "DVein Bank Team",
                customerName,
                month,
                accountNumber,
                transactionCount,
                totalDebits,
                totalCredits,
                closingBalance
        );
    }
}