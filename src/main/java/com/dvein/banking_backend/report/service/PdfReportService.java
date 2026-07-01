// report/service/PdfReportService.java
package com.dvein.banking_backend.report.service;

import com.dvein.banking_backend.report.model.ReportRequest;
import com.dvein.banking_backend.report.enums.ReportType;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.transaction.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import java.time.LocalDate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfReportService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public byte[] generatePdf(ReportRequest reportRequest) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Add header
            addHeader(document, reportRequest);

            // Add content based on report type
            switch (reportRequest.getReportType()) {
                case ACCOUNT_STATEMENT:
                    addAccountStatement(document, reportRequest);
                    break;
                case TRANSACTION_HISTORY:
                    addTransactionHistory(document, reportRequest);
                    break;
                case MINI_STATEMENT:
                    addMiniStatement(document, reportRequest);
                    break;
                // Add other report types...
                default:
                    addDefaultContent(document, reportRequest);
            }

            // Add footer
            addFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (DocumentException e) {
            log.error("Error generating PDF report: {}", reportRequest.getId(), e);
            throw new RuntimeException("Error generating PDF report", e);
        }
    }

    private void addHeader(Document document, ReportRequest reportRequest) throws DocumentException {
        Table headerTable = new Table(2);
        headerTable.setWidth(100);

        Cell bankName = new Cell(new Phrase("BANK NAME", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        bankName.setBorder(0);
        headerTable.addCell(bankName);

        Cell reportTitle = new Cell(new Phrase(
                reportRequest.getReportType().toString().replace("_", " "),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)
        ));
        reportTitle.setHorizontalAlignment(Element.ALIGN_RIGHT);
        reportTitle.setBorder(0);
        headerTable.addCell(reportTitle);

        document.add(headerTable);
        document.add(new Paragraph(" "));

        // Add report details
        document.add(new Paragraph(
                String.format("Report ID: %s", reportRequest.getId()),
                FontFactory.getFont(FontFactory.HELVETICA, 10)
        ));
        String generatedOn = reportRequest.getGeneratedAt() != null
                ? reportRequest.getGeneratedAt()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
                : "In Progress";

        document.add(new Paragraph(
                "Generated on: " + generatedOn,
                FontFactory.getFont(FontFactory.HELVETICA, 10)
        ));
        document.add(new Paragraph(" "));
    }

    private void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph(
                "This is an auto-generated report. Please verify the details before using for any official purpose.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9)
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private void addAccountStatement(Document document, ReportRequest reportRequest)
            throws DocumentException {

        Long customerId = Long.valueOf(reportRequest.getRequestedBy());

        List<Account> accounts =
                accountRepository.findAllByCustomerId(customerId);

        if (accounts.isEmpty()) {
            throw new RuntimeException("No accounts found for customer");
        }

        // Customer Name (printed once)
        document.add(new Paragraph(
                "Customer Name : " +
                        accounts.get(0).getCustomer().getFullName(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));

        document.add(new Paragraph(" "));

        // Handle missing dates safely
        LocalDate startDate = reportRequest.getStartDate() != null
                ? reportRequest.getStartDate()
                : LocalDate.now().minusMonths(1);

        LocalDate endDate = reportRequest.getEndDate() != null
                ? reportRequest.getEndDate()
                : LocalDate.now();

        // Statement Period (printed once)
        document.add(new Paragraph(
                "Statement Period : "
                        + startDate
                        + " to "
                        + endDate,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));

        document.add(new Paragraph(" "));

        // Loop through all accounts
        for (Account account : accounts) {

            // Account details title with account type
            Paragraph title = new Paragraph(
                    account.getAccountType() + " ACCOUNT",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));

            document.add(title);

            document.add(new Paragraph("---------------------------------------------"));

            document.add(new Paragraph(
                    "Account Number : " + account.getAccountNumber()));

            document.add(new Paragraph(
                    "Account Type : " + account.getAccountType()));

            document.add(new Paragraph(
                    "IFSC : " + account.getIfscCode()));

            document.add(new Paragraph(
                    "Branch : " + account.getBranchName()));

            document.add(new Paragraph(
                    "Current Balance : ₹" + account.getBalance()));

            document.add(new Paragraph(" "));

            // Get transactions for this account with safe date handling
            List<Transaction> transactions =
                    transactionRepository.findByAccountAndDateRange(
                            account,
                            startDate.atStartOfDay(),
                            endDate.atTime(23, 59, 59)
                    );

            if (transactions.isEmpty()) {

                document.add(new Paragraph(
                        "No transactions found."));

            } else {

                addTransactionTable(document, transactions, account);
            }

            document.add(new Paragraph(" "));
        }
    }

    private void addTransactionTable(Document document,
                                     List<Transaction> transactions,
                                     Account account)
            throws DocumentException {

        Table table = new Table(6);
        table.setWidth(100);

        table.addCell(headerCell("Date"));
        table.addCell(headerCell("Description"));
        table.addCell(headerCell("Type"));
        table.addCell(headerCell("Debit"));
        table.addCell(headerCell("Credit"));
        table.addCell(headerCell("Balance"));

        java.math.BigDecimal totalDebit = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalCredit = java.math.BigDecimal.ZERO;

        for (Transaction tx : transactions) {

            table.addCell(normalCell(
                    tx.getInitiatedAt() == null
                            ? "-"
                            : tx.getInitiatedAt().format(
                            DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            ));

            table.addCell(normalCell(
                    tx.getDescription() == null
                            ? "-"
                            : tx.getDescription()
            ));

            table.addCell(normalCell(
                    tx.getTransactionType().toString()
            ));

            boolean debit =
                    tx.getSenderAccount() != null &&
                            tx.getSenderAccount().getId().equals(account.getId());

            if (debit) {

                table.addCell(normalCell("₹" + tx.getAmount()));
                table.addCell(normalCell("-"));

                totalDebit = totalDebit.add(tx.getAmount());

                table.addCell(normalCell(
                        tx.getSenderBalanceAfter() == null
                                ? "-"
                                : "₹" + tx.getSenderBalanceAfter()
                ));

            } else {

                table.addCell(normalCell("-"));
                table.addCell(normalCell("₹" + tx.getAmount()));

                totalCredit = totalCredit.add(tx.getAmount());

                table.addCell(normalCell(
                        tx.getReceiverBalanceAfter() == null
                                ? "-"
                                : "₹" + tx.getReceiverBalanceAfter()
                ));
            }
        }

        document.add(table);

        document.add(new Paragraph(" "));

        document.add(new Paragraph(
                "Total Debit : ₹" + totalDebit,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));

        document.add(new Paragraph(
                "Total Credit : ₹" + totalCredit,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));

        document.add(new Paragraph(
                "Closing Balance : ₹" + account.getBalance(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
    }

    private Cell headerCell(String text) {

        Cell cell = new Cell(
                new Phrase(
                        text,
                        FontFactory.getFont(
                                FontFactory.HELVETICA_BOLD,
                                10)));

        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        return cell;
    }

    private Cell normalCell(String text) {

        Cell cell = new Cell(
                new Phrase(
                        text,
                        FontFactory.getFont(
                                FontFactory.HELVETICA,
                                9)));

        return cell;
    }

    private void addTransactionHistory(Document document,
                                       ReportRequest reportRequest)
            throws DocumentException {

        addAccountStatement(document, reportRequest);
    }

    private void addMiniStatement(Document document, ReportRequest reportRequest) throws DocumentException {
        // TODO: Implement mini statement PDF generation
        document.add(new Paragraph("Mini Statement", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
    }

    private void addDefaultContent(Document document, ReportRequest reportRequest) throws DocumentException {
        document.add(new Paragraph("Report Content", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
    }
}