package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.notification.service.EmailService;
import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionNotificationService {

    private final EmailService emailService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    @Async
    public void sendTransactionNotification(Transaction transaction) {
        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            sendDebitNotification(transaction);
            sendCreditNotification(transaction);
        } else if (transaction.getStatus() == TransactionStatus.FAILED) {
            sendFailureNotification(transaction);
        }
    }

    private void sendDebitNotification(Transaction transaction) {
        if (transaction.getSenderAccount() == null) {
            return;
        }

        String email = transaction.getSenderAccount().getCustomer().getUser().getEmail();
        String subject = "Money Debited - DVein Bank";

        String message = buildDebitMessage(transaction);

        emailService.sendOtpEmail(email, subject, message);
        log.info("Debit notification sent to: {}", email);
    }

    private void sendCreditNotification(Transaction transaction) {
        if (transaction.getReceiverAccount() == null) {
            return;
        }

        String email = transaction.getReceiverAccount().getCustomer().getUser().getEmail();
        String subject = "Money Credited - DVein Bank";

        String message = buildCreditMessage(transaction);

        emailService.sendOtpEmail(email, subject, message);
        log.info("Credit notification sent to: {}", email);
    }

    private void sendFailureNotification(Transaction transaction) {
        if (transaction.getSenderAccount() == null) {
            return;
        }

        String email = transaction.getSenderAccount().getCustomer().getUser().getEmail();
        String subject = "Transaction Failed - DVein Bank";

        String message = buildFailureMessage(transaction);

        emailService.sendOtpEmail(email, subject, message);
        log.info("Failure notification sent to: {}", email);
    }

    private String buildDebitMessage(Transaction transaction) {
        return String.format(
                "Dear Customer,\n\n" +
                        "Your account has been debited.\n\n" +
                        "Transaction ID: %s\n" +
                        "Amount: Rs. %.2f\n" +
                        "To: %s\n" +
                        "Date: %s\n" +
                        "Balance: Rs. %.2f\n\n" +
                        "If you did not authorize this transaction, please contact us immediately.\n\n" +
                        "DVein Bank",
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getReceiverName() != null ? transaction.getReceiverName() :
                        (transaction.getReceiverAccount() != null ? transaction.getReceiverAccount().getAccountNumber() : "N/A"),
                transaction.getCompletedAt().format(DATE_FORMATTER),
                transaction.getSenderBalanceAfter()
        );
    }

    private String buildCreditMessage(Transaction transaction) {
        return String.format(
                "Dear Customer,\n\n" +
                        "Your account has been credited.\n\n" +
                        "Transaction ID: %s\n" +
                        "Amount: Rs. %.2f\n" +
                        "From: %s\n" +
                        "Date: %s\n" +
                        "Balance: Rs. %.2f\n\n" +
                        "DVein Bank",
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getSenderAccount() != null ?
                        transaction.getSenderAccount().getCustomer().getFullName() : "External",
                transaction.getCompletedAt().format(DATE_FORMATTER),
                transaction.getReceiverBalanceAfter()
        );
    }

    private String buildFailureMessage(Transaction transaction) {
        return String.format(
                "Dear Customer,\n\n" +
                        "Your transaction has failed.\n\n" +
                        "Transaction ID: %s\n" +
                        "Amount: Rs. %.2f\n" +
                        "Reason: %s\n" +
                        "Date: %s\n\n" +
                        "Please try again or contact customer support.\n\n" +
                        "DVein Bank",
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getFailureReason(),
                transaction.getFailedAt().format(DATE_FORMATTER)
        );
    }

    @Async
    public void sendFraudAlert(String email, String transactionId, String reason) {
        String subject = "Security Alert - Suspicious Transaction Blocked";

        String message = String.format(
                "Dear Customer,\n\n" +
                        "We detected suspicious activity on your account.\n\n" +
                        "Transaction ID: %s\n" +
                        "Reason: %s\n\n" +
                        "This transaction has been blocked for your security.\n" +
                        "If you attempted this transaction, please contact customer support.\n" +
                        "If not, please change your password immediately.\n\n" +
                        "DVein Bank Security Team",
                transactionId,
                reason
        );

        emailService.sendSecurityAlert(email, reason, "N/A", "N/A");
        log.info("Fraud alert sent to: {}", email);
    }
}