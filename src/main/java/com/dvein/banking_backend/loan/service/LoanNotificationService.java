package com.dvein.banking_backend.loan.service;

import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.loan.model.Loan;
import com.dvein.banking_backend.loan.model.LoanRepayment;
import com.dvein.banking_backend.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanNotificationService {

    private final EmailService emailService;

    public void sendLoanApplicationNotification(User user, Loan loan) {
        try {
            String content = String.format("""
                Dear %s,
                
                Your loan application has been received successfully.
                
                Loan Details:
                Loan Number: %s
                Loan Type: %s
                Amount: ₹%s
                Tenure: %d months
                EMI: ₹%s
                Status: %s
                
                We will review your application and update you shortly.
                
                Thank you for choosing us.
                """,
                    user.getEmail(),
                    loan.getLoanNumber(),
                    loan.getLoanType(),
                    loan.getPrincipalAmount(),
                    loan.getTenureMonths(),
                    loan.getEmiAmount(),
                    loan.getStatus()
            );

            emailService.sendEmail(user.getEmail(), "Loan Application Received - " + loan.getLoanNumber(), content);
        } catch (Exception e) {
            log.error("Failed to send loan application notification", e);
        }
    }

    public void sendLoanApprovalNotification(User user, Loan loan) {
        try {
            String content = String.format("""
                Dear %s,
                
                Congratulations! Your loan application has been APPROVED.
                
                Loan Details:
                Loan Number: %s
                Approved Amount: ₹%s
                Interest Rate: %s%%
                Tenure: %d months
                EMI: ₹%s
                
                The amount will be disbursed to your account shortly.
                
                Thank you for choosing us.
                """,
                    user.getEmail(),
                    loan.getLoanNumber(),
                    loan.getPrincipalAmount(),
                    loan.getInterestRate(),
                    loan.getTenureMonths(),
                    loan.getEmiAmount()
            );

            emailService.sendEmail(user.getEmail(), "Loan Approved - " + loan.getLoanNumber(), content);
        } catch (Exception e) {
            log.error("Failed to send loan approval notification", e);
        }
    }

    public void sendLoanRejectionNotification(User user, Loan loan) {
        try {
            String content = String.format("""
                Dear %s,
                
                We regret to inform you that your loan application has been rejected.
                
                Loan Number: %s
                Reason: %s
                
                You may reapply after addressing the above concerns.
                
                Thank you for your understanding.
                """,
                    user.getEmail(),
                    loan.getLoanNumber(),
                    loan.getRejectionReason()
            );

            emailService.sendEmail(user.getEmail(), "Loan Application Status - " + loan.getLoanNumber(), content);
        } catch (Exception e) {
            log.error("Failed to send loan rejection notification", e);
        }
    }

    public void sendLoanDisbursementNotification(User user, Loan loan) {
        try {
            String content = String.format("""
                Dear %s,
                
                Your loan amount has been disbursed successfully!
                
                Loan Number: %s
                Disbursed Amount: ₹%s
                First EMI Date: %s
                EMI Amount: ₹%s
                
                Please ensure timely EMI payments.
                
                Thank you for banking with us.
                """,
                    user.getEmail(),
                    loan.getLoanNumber(),
                    loan.getPrincipalAmount(),
                    loan.getFirstEmiDate(),
                    loan.getEmiAmount()
            );

            emailService.sendEmail(user.getEmail(), "Loan Disbursed - " + loan.getLoanNumber(), content);
        } catch (Exception e) {
            log.error("Failed to send loan disbursement notification", e);
        }
    }

    public void sendEmiPaidNotification(User user, Loan loan, LoanRepayment repayment) {
        try {
            String content = String.format("""
                Dear %s,
                
                Your EMI payment has been received successfully.
                
                Loan Number: %s
                Payment Amount: ₹%s
                Principal Paid: ₹%s
                Interest Paid: ₹%s
                Remaining Balance: ₹%s
                Transaction ID: %s
                
                Thank you for your payment.
                """,
                    user.getEmail(),
                    loan.getLoanNumber(),
                    repayment.getPaymentAmount(),
                    repayment.getPrincipalPaid(),
                    repayment.getInterestPaid(),
                    repayment.getRemainingBalance(),
                    repayment.getTransactionId()
            );

            emailService.sendEmail(user.getEmail(), "EMI Payment Received - " + loan.getLoanNumber(), content);
        } catch (Exception e) {
            log.error("Failed to send EMI paid notification", e);
        }
    }

    public void sendLoanClosureNotification(User user, Loan loan) {
        try {
            String content = String.format("""
                Dear %s,
                
                Congratulations! Your loan has been fully paid and CLOSED.
                
                Loan Number: %s
                Closure Date: %s
                Total Amount Paid: ₹%s
                
                Thank you for your timely payments. We look forward to serving you again.
                """,
                    user.getEmail(),
                    loan.getLoanNumber(),
                    loan.getClosedDate(),
                    loan.getAmountPaid()
            );

            emailService.sendEmail(user.getEmail(), "Loan Closed - " + loan.getLoanNumber(), content);
        } catch (Exception e) {
            log.error("Failed to send loan closure notification", e);
        }
    }

    public void sendEmiReminderNotification(User user, Loan loan) {
        try {
            String content = String.format("""
                Dear %s,
                
                This is a reminder that your EMI is due soon.
                
                Loan Number: %s
                EMI Amount: ₹%s
                
                Please ensure sufficient balance in your account.
                
                Thank you.
                """,
                    user.getEmail(),
                    loan.getLoanNumber(),
                    loan.getEmiAmount()
            );

            emailService.sendEmail(user.getEmail(), "EMI Reminder - " + loan.getLoanNumber(), content);
        } catch (Exception e) {
            log.error("Failed to send EMI reminder notification", e);
        }
    }
}