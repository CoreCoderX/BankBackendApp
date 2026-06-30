package com.dvein.banking_backend.reports.service;

import com.dvein.banking_backend.reports.dto.request.ReportRequestDTO;
import com.dvein.banking_backend.reports.dto.response.AccountStatementDataDTO;
import com.dvein.banking_backend.reports.dto.response.FinancialSummaryReportDTO;
import com.dvein.banking_backend.reports.dto.response.LoanReportDataDTO;
import com.dvein.banking_backend.reports.dto.response.ReportRequestResponseDTO;
import com.dvein.banking_backend.reports.dto.response.TransactionReportDataDTO;
import com.dvein.banking_backend.reports.entity.*;
import com.dvein.banking_backend.reports.exception.ReportException;
import com.dvein.banking_backend.reports.repository.*;
import com.dvein.banking_backend.account.repository.CustomerRepository;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.admin.model.AuditLog;
import com.dvein.banking_backend.admin.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Apache POI for Excel
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// OpenPDF for PDF generation
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRequestRepository    reportRequestRepository;
    private final ReportDefinitionRepository reportDefinitionRepository;
    private final CustomerRepository         customerRepository;
    private final AccountRepository          accountRepository;
    private final AuditLogRepository         auditLogRepository;
    private final ReportDataQueryService     reportDataQueryService;

    private static final String REPORTS_DIR = "reports/generated/";

    // ── Request Report ────────────────────────────────────────────────────────

    @Transactional
    public ReportRequestResponseDTO requestReport(ReportRequestDTO dto,
                                                  Long requestedBy,
                                                  RequesterRole role) {
        ReportDefinition definition = reportDefinitionRepository
                .findByReportCode(dto.getReportCode())
                .orElseThrow(() -> new ReportException(
                        "Report type not found: " + dto.getReportCode()));

        if (definition.getRequiresDateRange()) {
            if (dto.getFromDate() == null || dto.getToDate() == null) {
                throw new ReportException(
                        "Date range is required for this report type");
            }
            if (dto.getFromDate().isAfter(dto.getToDate())) {
                throw new ReportException(
                        "From date cannot be after to date");
            }
        }

        ReportRequest request = ReportRequest.builder()
                .reportDefinition(definition)
                .requestedBy(requestedBy)
                .requesterRole(role)
                .reportType(definition.getReportCode())
                .reportFormat(dto.getReportFormat())
                .status(ReportStatus.PENDING)
                .fromDate(dto.getFromDate())
                .toDate(dto.getToDate())
                .accountId(dto.getAccountId())
                .customerId(dto.getCustomerId())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .downloadCount(0)
                .build();

        ReportRequest saved = reportRequestRepository.save(request);
        generateReportAsync(saved.getId());

        log.info("Report request created: ID={}, Type={}, Format={}, User={}",
                saved.getId(), dto.getReportCode(), dto.getReportFormat(), requestedBy);

        return mapToResponse(saved);
    }

    // ── Async Generation ──────────────────────────────────────────────────────

    @Async("reportTaskExecutor")
    public void generateReportAsync(Long reportRequestId) {
        log.info("Async generation started for report ID: {}", reportRequestId);

        ReportRequest request = reportRequestRepository
                .findById(reportRequestId)
                .orElse(null);

        if (request == null) {
            log.error("Report request not found: {}", reportRequestId);
            return;
        }

        request.setStatus(ReportStatus.PROCESSING);
        request.setProcessingStartedAt(LocalDateTime.now());
        reportRequestRepository.save(request);

        try {
            Files.createDirectories(Paths.get(REPORTS_DIR));

            String filePath;

            switch (request.getReportFormat()) {
                case PDF:
                    filePath = generatePdf(request);
                    break;
                case EXCEL:
                    filePath = generateExcel(request);
                    break;
                case CSV:
                default:
                    filePath = generateCsv(request);
                    break;
            }

            request.setStatus(ReportStatus.COMPLETED);
            request.setFileName(Paths.get(filePath).getFileName().toString());
            request.setFilePath(filePath);
            request.setFileSizeBytes(Files.size(Paths.get(filePath)));
            request.setCompletedAt(LocalDateTime.now());

            log.info("Report generated successfully: {}", filePath);

        } catch (Exception e) {
            log.error("Report generation failed: {}", e.getMessage(), e);
            request.setStatus(ReportStatus.FAILED);
            request.setErrorMessage(e.getMessage());
            request.setCompletedAt(LocalDateTime.now());
        }

        reportRequestRepository.save(request);
    }

    // ── CSV Generation ────────────────────────────────────────────────────────

    private String generateCsv(ReportRequest request) throws IOException {
        String fileName = String.format("%s_%d_%d.csv",
                request.getReportType(),
                request.getId(),
                System.currentTimeMillis());

        String filePath = REPORTS_DIR + fileName;
        Files.writeString(Paths.get(filePath), getReportContent(request));
        return filePath;
    }

    // ── Excel Generation ──────────────────────────────────────────────────────

    private String generateExcel(ReportRequest request) throws IOException {
        String fileName = String.format("%s_%d_%d.xlsx",
                request.getReportType(),
                request.getId(),
                System.currentTimeMillis());

        String filePath = REPORTS_DIR + fileName;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(request.getReportType());

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            switch (request.getReportType()) {
                case "CUSTOMER_REPORT":
                    buildCustomerExcel(sheet, headerStyle);
                    break;
                case "ACCOUNT_REPORT":
                    buildAccountExcel(sheet, headerStyle);
                    break;
                case "AUDIT_REPORT":
                    buildAuditExcel(sheet, headerStyle);
                    break;
                case "LOAN_STATEMENT":
                    buildLoanExcel(sheet, headerStyle, request);
                    break;
                case "TRANSACTION_REPORT":
                    buildTransactionExcel(sheet, headerStyle, request);
                    break;
                case "ACCOUNT_STATEMENT":
                    buildAccountStatementExcel(sheet, headerStyle, request);
                    break;
                case "FINANCIAL_SUMMARY":
                    buildFinancialSummaryExcel(sheet, headerStyle, request);
                    break;
                default:
                    buildSimpleExcel(sheet, request, headerStyle);
            }

            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }

        return filePath;
    }

    // ── PDF Generation ────────────────────────────────────────────────────────

    private String generatePdf(ReportRequest request) throws Exception {
        String fileName = String.format("%s_%d_%d.pdf",
                request.getReportType(),
                request.getId(),
                System.currentTimeMillis());

        String filePath = REPORTS_DIR + fileName;

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        com.lowagie.text.Font titleFont =
                new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18,
                        com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font headerFont =
                new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12,
                        com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font normalFont =
                new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10,
                        com.lowagie.text.Font.NORMAL);

        Paragraph title = new Paragraph("DVein Bank Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Report Type: " + request.getReportType(), headerFont));
        document.add(new Paragraph("Generated At: " + LocalDateTime.now(), normalFont));
        document.add(new Paragraph(" "));

        switch (request.getReportType()) {
            case "CUSTOMER_REPORT":
                buildCustomerPdf(document);
                break;
            case "ACCOUNT_REPORT":
                buildAccountPdf(document);
                break;
            case "AUDIT_REPORT":
                buildAuditPdf(document);
                break;
            case "LOAN_STATEMENT":
                buildLoanPdf(document, request);
                break;
            case "TRANSACTION_REPORT":
                buildTransactionPdf(document, request);
                break;
            case "ACCOUNT_STATEMENT":
                buildAccountStatementPdf(document, request);
                break;
            case "FINANCIAL_SUMMARY":
                buildFinancialSummaryPdf(document, request);
                break;
            default:
                buildSimplePdf(document, request);
        }

        document.close();
        return filePath;
    }

    // ── CSV Content Router ────────────────────────────────────────────────────

    private String getReportContent(ReportRequest request) {
        switch (request.getReportType()) {
            case "CUSTOMER_REPORT":
                return buildCustomerReport();
            case "ACCOUNT_REPORT":
                return buildAccountReport();
            case "AUDIT_REPORT":
                return buildAuditReport();
            case "LOAN_STATEMENT":
                return buildLoanCsv(request);
            case "TRANSACTION_REPORT":
                return buildTransactionCsv(request);
            case "ACCOUNT_STATEMENT":
                return buildAccountStatementCsv(request);
            case "FINANCIAL_SUMMARY":
                return buildFinancialSummaryCsv(request);
            default:
                return buildSimpleCsvContent(request);
        }
    }

    // =========================================================================
    // ── LOAN STATEMENT ────────────────────────────────────────────────────────
    // =========================================================================

    private void buildLoanPdf(Document document, ReportRequest request)
            throws DocumentException {

        LoanReportDataDTO data = reportDataQueryService.getLoanReportData(
                request.getFromDate(), request.getToDate());

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        addPdfRow(table, "Total Loans",          safeStr(data.getTotalLoans()));
        addPdfRow(table, "Active Loans",          safeStr(data.getActiveLoans()));
        addPdfRow(table, "Closed Loans",          safeStr(data.getClosedLoans()));
        addPdfRow(table, "Pending Loans",         safeStr(data.getPendingApprovalLoans()));
        addPdfRow(table, "Defaulted Loans",       safeStr(data.getDefaultedLoans()));
        addPdfRow(table, "Total Disbursed",       safeStr(data.getTotalDisbursedAmount()));
        addPdfRow(table, "Outstanding Amount",    safeStr(data.getTotalOutstandingAmount()));
        addPdfRow(table, "Total Repaid",          safeStr(data.getTotalRepaidAmount()));
        addPdfRow(table, "Interest Earned",       safeStr(data.getTotalInterestEarned()));
        addPdfRow(table, "Default Rate",          safeStr(data.getDefaultRate()) + "%");

        document.add(table);
    }

    private void buildLoanExcel(Sheet sheet, CellStyle headerStyle,
                                ReportRequest request) {

        LoanReportDataDTO data = reportDataQueryService.getLoanReportData(
                request.getFromDate(), request.getToDate());

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < 2; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(i == 0 ? "Metric" : "Value");
            cell.setCellStyle(headerStyle);
        }

        rowNum = addExcelRow(sheet, rowNum, "Total Loans",         safeStr(data.getTotalLoans()));
        rowNum = addExcelRow(sheet, rowNum, "Active Loans",         safeStr(data.getActiveLoans()));
        rowNum = addExcelRow(sheet, rowNum, "Closed Loans",         safeStr(data.getClosedLoans()));
        rowNum = addExcelRow(sheet, rowNum, "Pending Loans",        safeStr(data.getPendingApprovalLoans()));
        rowNum = addExcelRow(sheet, rowNum, "Defaulted Loans",      safeStr(data.getDefaultedLoans()));
        rowNum = addExcelRow(sheet, rowNum, "Total Disbursed",      safeStr(data.getTotalDisbursedAmount()));
        rowNum = addExcelRow(sheet, rowNum, "Outstanding Amount",   safeStr(data.getTotalOutstandingAmount()));
        rowNum = addExcelRow(sheet, rowNum, "Total Repaid",         safeStr(data.getTotalRepaidAmount()));
        rowNum = addExcelRow(sheet, rowNum, "Interest Earned",      safeStr(data.getTotalInterestEarned()));
        addExcelRow(sheet, rowNum,          "Default Rate",         safeStr(data.getDefaultRate()) + "%");
    }

    private String buildLoanCsv(ReportRequest request) {
        LoanReportDataDTO data = reportDataQueryService.getLoanReportData(
                request.getFromDate(), request.getToDate());

        StringBuilder sb = new StringBuilder();
        sb.append("Metric,Value\n");
        sb.append("Total Loans,").append(data.getTotalLoans()).append("\n");
        sb.append("Active Loans,").append(data.getActiveLoans()).append("\n");
        sb.append("Closed Loans,").append(data.getClosedLoans()).append("\n");
        sb.append("Pending Loans,").append(data.getPendingApprovalLoans()).append("\n");
        sb.append("Defaulted Loans,").append(data.getDefaultedLoans()).append("\n");
        sb.append("Total Disbursed,").append(data.getTotalDisbursedAmount()).append("\n");
        sb.append("Outstanding Amount,").append(data.getTotalOutstandingAmount()).append("\n");
        sb.append("Total Repaid,").append(data.getTotalRepaidAmount()).append("\n");
        sb.append("Interest Earned,").append(data.getTotalInterestEarned()).append("\n");
        sb.append("Default Rate,").append(data.getDefaultRate()).append("%\n");
        return sb.toString();
    }

    // =========================================================================
    // ── TRANSACTION REPORT ────────────────────────────────────────────────────
    // =========================================================================

    private void buildTransactionPdf(Document document, ReportRequest request)
            throws DocumentException {

        TransactionReportDataDTO data = reportDataQueryService.getTransactionSummary(
                request.getFromDate(), request.getToDate());

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        addPdfRow(table, "Report Date",             safeStr(data.getReportDate()));
        addPdfRow(table, "Total Transactions",       safeStr(data.getTotalTransactions()));
        addPdfRow(table, "Total Credit Count",       safeStr(data.getTotalCreditCount()));
        addPdfRow(table, "Total Debit Count",        safeStr(data.getTotalDebitCount()));
        addPdfRow(table, "Total Credit Amount",      safeStr(data.getTotalCreditAmount()));
        addPdfRow(table, "Total Debit Amount",       safeStr(data.getTotalDebitAmount()));
        addPdfRow(table, "Net Amount",               safeStr(data.getNetAmount()));
        addPdfRow(table, "Average Transaction",      safeStr(data.getAverageTransactionAmount()));
        addPdfRow(table, "Highest Transaction",      safeStr(data.getHighestTransaction()));
        addPdfRow(table, "Lowest Transaction",       safeStr(data.getLowestTransaction()));

        document.add(table);
    }

    private void buildTransactionExcel(Sheet sheet, CellStyle headerStyle,
                                       ReportRequest request) {

        TransactionReportDataDTO data = reportDataQueryService.getTransactionSummary(
                request.getFromDate(), request.getToDate());

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < 2; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(i == 0 ? "Metric" : "Value");
            cell.setCellStyle(headerStyle);
        }

        rowNum = addExcelRow(sheet, rowNum, "Report Date",          safeStr(data.getReportDate()));
        rowNum = addExcelRow(sheet, rowNum, "Total Transactions",    safeStr(data.getTotalTransactions()));
        rowNum = addExcelRow(sheet, rowNum, "Total Credit Count",    safeStr(data.getTotalCreditCount()));
        rowNum = addExcelRow(sheet, rowNum, "Total Debit Count",     safeStr(data.getTotalDebitCount()));
        rowNum = addExcelRow(sheet, rowNum, "Total Credit Amount",   safeStr(data.getTotalCreditAmount()));
        rowNum = addExcelRow(sheet, rowNum, "Total Debit Amount",    safeStr(data.getTotalDebitAmount()));
        rowNum = addExcelRow(sheet, rowNum, "Net Amount",            safeStr(data.getNetAmount()));
        rowNum = addExcelRow(sheet, rowNum, "Average Transaction",   safeStr(data.getAverageTransactionAmount()));
        rowNum = addExcelRow(sheet, rowNum, "Highest Transaction",   safeStr(data.getHighestTransaction()));
        addExcelRow(sheet, rowNum,          "Lowest Transaction",    safeStr(data.getLowestTransaction()));
    }

    private String buildTransactionCsv(ReportRequest request) {
        TransactionReportDataDTO data = reportDataQueryService.getTransactionSummary(
                request.getFromDate(), request.getToDate());

        StringBuilder sb = new StringBuilder();
        sb.append("Metric,Value\n");
        sb.append("Report Date,").append(data.getReportDate()).append("\n");
        sb.append("Total Transactions,").append(data.getTotalTransactions()).append("\n");
        sb.append("Total Credit Count,").append(data.getTotalCreditCount()).append("\n");
        sb.append("Total Debit Count,").append(data.getTotalDebitCount()).append("\n");
        sb.append("Total Credit Amount,").append(data.getTotalCreditAmount()).append("\n");
        sb.append("Total Debit Amount,").append(data.getTotalDebitAmount()).append("\n");
        sb.append("Net Amount,").append(data.getNetAmount()).append("\n");
        sb.append("Average Transaction,").append(data.getAverageTransactionAmount()).append("\n");
        sb.append("Highest Transaction,").append(data.getHighestTransaction()).append("\n");
        sb.append("Lowest Transaction,").append(data.getLowestTransaction()).append("\n");
        return sb.toString();
    }

    // =========================================================================
    // ── ACCOUNT STATEMENT ─────────────────────────────────────────────────────
    // =========================================================================

    private void buildAccountStatementPdf(Document document, ReportRequest request)
            throws DocumentException {

        Long accountId = request.getAccountId();
        List<Map<String, Object>> rows = reportDataQueryService.getAccountStatementData(
                accountId, request.getFromDate(), request.getToDate());

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);

        // Headers
        for (String h : new String[]{"Date", "Type", "Direction", "Amount",
                "Balance After", "Description", "Reference"}) {
            table.addCell(h);
        }

        // Data
        for (Map<String, Object> row : rows) {
            table.addCell(safeStr(row.get("transaction_date")));
            table.addCell(safeStr(row.get("transaction_type")));
            table.addCell(safeStr(row.get("transaction_direction")));
            table.addCell(safeStr(row.get("amount")));
            table.addCell(safeStr(row.get("balance_after")));
            table.addCell(safeStr(row.get("description")));
            table.addCell(safeStr(row.get("reference_number")));
        }

        document.add(table);
    }

    private void buildAccountStatementExcel(Sheet sheet, CellStyle headerStyle,
                                            ReportRequest request) {

        Long accountId = request.getAccountId();
        List<Map<String, Object>> rows = reportDataQueryService.getAccountStatementData(
                accountId, request.getFromDate(), request.getToDate());

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Date", "Type", "Direction", "Amount",
                "Balance After", "Description", "Reference"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (Map<String, Object> row : rows) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(safeStr(row.get("transaction_date")));
            dataRow.createCell(1).setCellValue(safeStr(row.get("transaction_type")));
            dataRow.createCell(2).setCellValue(safeStr(row.get("transaction_direction")));
            dataRow.createCell(3).setCellValue(safeStr(row.get("amount")));
            dataRow.createCell(4).setCellValue(safeStr(row.get("balance_after")));
            dataRow.createCell(5).setCellValue(safeStr(row.get("description")));
            dataRow.createCell(6).setCellValue(safeStr(row.get("reference_number")));
        }
    }

    private String buildAccountStatementCsv(ReportRequest request) {
        Long accountId = request.getAccountId();
        List<Map<String, Object>> rows = reportDataQueryService.getAccountStatementData(
                accountId, request.getFromDate(), request.getToDate());

        StringBuilder sb = new StringBuilder();
        sb.append("Date,Type,Direction,Amount,Balance After,Description,Reference\n");
        for (Map<String, Object> row : rows) {
            sb.append(safeStr(row.get("transaction_date"))).append(",");
            sb.append(safeStr(row.get("transaction_type"))).append(",");
            sb.append(safeStr(row.get("transaction_direction"))).append(",");
            sb.append(safeStr(row.get("amount"))).append(",");
            sb.append(safeStr(row.get("balance_after"))).append(",");
            sb.append(escapeCSV(safeStr(row.get("description")))).append(",");
            sb.append(safeStr(row.get("reference_number"))).append("\n");
        }
        return sb.toString();
    }

    // =========================================================================
    // ── FINANCIAL SUMMARY ─────────────────────────────────────────────────────
    // =========================================================================

    private void buildFinancialSummaryPdf(Document document, ReportRequest request)
            throws DocumentException {

        FinancialSummaryReportDTO data = reportDataQueryService.getFinancialSummary(
                request.getFromDate(), request.getToDate());

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        addPdfRow(table, "Period",                   data.getFromDate() + " to " + data.getToDate());
        addPdfRow(table, "Total Deposits",            safeStr(data.getTotalDeposits()));
        addPdfRow(table, "Total Withdrawals",         safeStr(data.getTotalWithdrawals()));
        addPdfRow(table, "Total Transfers",           safeStr(data.getTotalTransferAmount()));
        addPdfRow(table, "Total Loan Disbursed",      safeStr(data.getTotalLoanDisbursed()));
        addPdfRow(table, "Total Interest Earned",     safeStr(data.getTotalInterestEarned()));
        addPdfRow(table, "Net Revenue",               safeStr(data.getNetRevenue()));
        addPdfRow(table, "Accounts Opened",           safeStr(data.getTotalAccountsOpened()));
        addPdfRow(table, "Accounts Closed",           safeStr(data.getTotalAccountsClosed()));
        addPdfRow(table, "Total Transaction Count",   safeStr(data.getTotalTransactionCount()));

        document.add(table);
    }

    private void buildFinancialSummaryExcel(Sheet sheet, CellStyle headerStyle,
                                            ReportRequest request) {

        FinancialSummaryReportDTO data = reportDataQueryService.getFinancialSummary(
                request.getFromDate(), request.getToDate());

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < 2; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(i == 0 ? "Metric" : "Value");
            cell.setCellStyle(headerStyle);
        }

        rowNum = addExcelRow(sheet, rowNum, "Period",
                data.getFromDate() + " to " + data.getToDate());
        rowNum = addExcelRow(sheet, rowNum, "Total Deposits",          safeStr(data.getTotalDeposits()));
        rowNum = addExcelRow(sheet, rowNum, "Total Withdrawals",       safeStr(data.getTotalWithdrawals()));
        rowNum = addExcelRow(sheet, rowNum, "Total Transfers",         safeStr(data.getTotalTransferAmount()));
        rowNum = addExcelRow(sheet, rowNum, "Total Loan Disbursed",    safeStr(data.getTotalLoanDisbursed()));
        rowNum = addExcelRow(sheet, rowNum, "Total Interest Earned",   safeStr(data.getTotalInterestEarned()));
        rowNum = addExcelRow(sheet, rowNum, "Net Revenue",             safeStr(data.getNetRevenue()));
        rowNum = addExcelRow(sheet, rowNum, "Accounts Opened",         safeStr(data.getTotalAccountsOpened()));
        rowNum = addExcelRow(sheet, rowNum, "Accounts Closed",         safeStr(data.getTotalAccountsClosed()));
        addExcelRow(sheet, rowNum,          "Total Transaction Count", safeStr(data.getTotalTransactionCount()));
    }

    private String buildFinancialSummaryCsv(ReportRequest request) {
        FinancialSummaryReportDTO data = reportDataQueryService.getFinancialSummary(
                request.getFromDate(), request.getToDate());

        StringBuilder sb = new StringBuilder();
        sb.append("Metric,Value\n");
        sb.append("Period,").append(data.getFromDate()).append(" to ").append(data.getToDate()).append("\n");
        sb.append("Total Deposits,").append(data.getTotalDeposits()).append("\n");
        sb.append("Total Withdrawals,").append(data.getTotalWithdrawals()).append("\n");
        sb.append("Total Transfers,").append(data.getTotalTransferAmount()).append("\n");
        sb.append("Total Loan Disbursed,").append(data.getTotalLoanDisbursed()).append("\n");
        sb.append("Total Interest Earned,").append(data.getTotalInterestEarned()).append("\n");
        sb.append("Net Revenue,").append(data.getNetRevenue()).append("\n");
        sb.append("Accounts Opened,").append(data.getTotalAccountsOpened()).append("\n");
        sb.append("Accounts Closed,").append(data.getTotalAccountsClosed()).append("\n");
        sb.append("Total Transaction Count,").append(data.getTotalTransactionCount()).append("\n");
        return sb.toString();
    }

    // =========================================================================
    // ── EXISTING CSV BUILDERS ────────────────────────────────────────────────
    // =========================================================================

    private String buildSimpleCsvContent(ReportRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Report Type,").append(request.getReportType()).append("\n");
        sb.append("Report ID,").append(request.getId()).append("\n");
        sb.append("From Date,").append(request.getFromDate()).append("\n");
        sb.append("To Date,").append(request.getToDate()).append("\n");
        sb.append("Generated At,").append(LocalDateTime.now()).append("\n");
        sb.append("Status,COMPLETED\n");
        return sb.toString();
    }

    private String buildCustomerReport() {
        List<Customer> customers = customerRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Customer ID,Full Name,Email,City,Status\n");
        for (Customer customer : customers) {
            String email = customer.getUser() != null
                    ? customer.getUser().getEmail() : "";
            sb.append(customer.getId()).append(",")
                    .append(escapeCSV(customer.getFullName())).append(",")
                    .append(email).append(",")
                    .append(escapeCSV(customer.getCity())).append(",")
                    .append(customer.getStatus())
                    .append("\n");
        }
        return sb.toString();
    }

    private String buildAccountReport() {
        List<Account> accounts = accountRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Account Number,Customer Name,Type,Balance,Status,Branch\n");
        for (Account account : accounts) {
            sb.append(account.getAccountNumber()).append(",")
                    .append(escapeCSV(account.getCustomer().getFullName())).append(",")
                    .append(account.getAccountType()).append(",")
                    .append(account.getBalance()).append(",")
                    .append(account.getStatus()).append(",")
                    .append(escapeCSV(account.getBranchName()))
                    .append("\n");
        }
        return sb.toString();
    }

    private String buildAuditReport() {
        List<AuditLog> logs = auditLogRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,User ID,Action,Entity,Description,IP Address,Date\n");
        for (AuditLog log : logs) {
            sb.append(log.getId()).append(",")
                    .append(log.getUserId()).append(",")
                    .append(log.getAction()).append(",")
                    .append(log.getEntityType()).append(",")
                    .append(escapeCSV(log.getDescription())).append(",")
                    .append(log.getIpAddress()).append(",")
                    .append(log.getCreatedAt())
                    .append("\n");
        }
        return sb.toString();
    }

    // =========================================================================
    // ── EXISTING EXCEL BUILDERS ───────────────────────────────────────────────
    // =========================================================================

    private int buildSimpleExcel(Sheet sheet, ReportRequest request,
                                 CellStyle headerStyle) {
        int rowNum = 0;
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Report Type");
        row.createCell(1).setCellValue(request.getReportType());

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Report ID");
        row.createCell(1).setCellValue(request.getId());

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Generated At");
        row.createCell(1).setCellValue(LocalDateTime.now().toString());

        return rowNum;
    }

    private int buildCustomerExcel(Sheet sheet, CellStyle headerStyle) {
        List<Customer> customers = customerRepository.findAll();
        int rowNum = 0;

        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Customer ID", "Full Name", "Email", "City", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (Customer customer : customers) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(customer.getId());
            row.createCell(1).setCellValue(customer.getFullName());
            row.createCell(2).setCellValue(customer.getUser() != null
                    ? customer.getUser().getEmail() : "");
            row.createCell(3).setCellValue(customer.getCity());
            row.createCell(4).setCellValue(customer.getStatus().toString());
        }

        return rowNum;
    }

    private int buildAccountExcel(Sheet sheet, CellStyle headerStyle) {
        List<Account> accounts = accountRepository.findAll();
        int rowNum = 0;

        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Account Number", "Customer Name", "Type",
                "Balance", "Status", "Branch"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (Account account : accounts) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(account.getAccountNumber());
            row.createCell(1).setCellValue(account.getCustomer().getFullName());
            row.createCell(2).setCellValue(account.getAccountType().toString());
            row.createCell(3).setCellValue(account.getBalance().doubleValue());
            row.createCell(4).setCellValue(account.getStatus().toString());
            row.createCell(5).setCellValue(account.getBranchName());
        }

        return rowNum;
    }

    private int buildAuditExcel(Sheet sheet, CellStyle headerStyle) {
        List<AuditLog> logs = auditLogRepository.findAll();
        int rowNum = 0;

        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "User ID", "Action", "Entity",
                "Description", "IP Address", "Date"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (AuditLog log : logs) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(log.getId());
            row.createCell(1).setCellValue(log.getUserId());
            row.createCell(2).setCellValue(log.getAction().toString());
            row.createCell(3).setCellValue(log.getEntityType().toString());
            row.createCell(4).setCellValue(log.getDescription());
            row.createCell(5).setCellValue(log.getIpAddress());
            row.createCell(6).setCellValue(log.getCreatedAt().toString());
        }

        return rowNum;
    }

    // =========================================================================
    // ── EXISTING PDF BUILDERS ─────────────────────────────────────────────────
    // =========================================================================

    private void buildSimplePdf(Document document, ReportRequest request)
            throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.addCell("Report Type");
        table.addCell(request.getReportType());
        table.addCell("Report ID");
        table.addCell(String.valueOf(request.getId()));
        table.addCell("Generated At");
        table.addCell(LocalDateTime.now().toString());
        document.add(table);
    }

    private void buildCustomerPdf(Document document) throws DocumentException {
        List<Customer> customers = customerRepository.findAll();
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.addCell("Customer ID");
        table.addCell("Full Name");
        table.addCell("Email");
        table.addCell("City");
        table.addCell("Status");
        for (Customer customer : customers) {
            table.addCell(String.valueOf(customer.getId()));
            table.addCell(customer.getFullName());
            table.addCell(customer.getUser() != null
                    ? customer.getUser().getEmail() : "");
            table.addCell(customer.getCity());
            table.addCell(customer.getStatus().toString());
        }
        document.add(table);
    }

    private void buildAccountPdf(Document document) throws DocumentException {
        List<Account> accounts = accountRepository.findAll();
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.addCell("Account Number");
        table.addCell("Customer Name");
        table.addCell("Type");
        table.addCell("Balance");
        table.addCell("Status");
        table.addCell("Branch");
        for (Account account : accounts) {
            table.addCell(account.getAccountNumber());
            table.addCell(account.getCustomer().getFullName());
            table.addCell(account.getAccountType().toString());
            table.addCell(account.getBalance().toString());
            table.addCell(account.getStatus().toString());
            table.addCell(account.getBranchName());
        }
        document.add(table);
    }

    private void buildAuditPdf(Document document) throws DocumentException {
        List<AuditLog> logs = auditLogRepository.findAll();
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.addCell("ID");
        table.addCell("User ID");
        table.addCell("Action");
        table.addCell("Entity");
        table.addCell("Description");
        table.addCell("IP Address");
        table.addCell("Date");
        for (AuditLog log : logs) {
            table.addCell(String.valueOf(log.getId()));
            table.addCell(String.valueOf(log.getUserId()));
            table.addCell(log.getAction().toString());
            table.addCell(log.getEntityType().toString());
            table.addCell(log.getDescription());
            table.addCell(log.getIpAddress());
            table.addCell(log.getCreatedAt().toString());
        }
        document.add(table);
    }

    // =========================================================================
    // ── SHARED HELPERS ────────────────────────────────────────────────────────
    // =========================================================================

    private void addPdfRow(PdfPTable table, String label, String value) {
        table.addCell(label);
        table.addCell(value != null ? value : "N/A");
    }

    private int addExcelRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value != null ? value : "N/A");
        return rowNum + 1;
    }

    private String safeStr(Object value) {
        return value == null ? "N/A" : value.toString();
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // =========================================================================
    // ── STATUS / DOWNLOAD / LISTING ───────────────────────────────────────────
    // =========================================================================

    @Transactional(readOnly = true)
    public ReportRequestResponseDTO getReportStatus(Long reportId, Long requestedBy) {
        ReportRequest request = reportRequestRepository
                .findByIdAndRequestedBy(reportId, requestedBy)
                .orElseThrow(() -> new ReportException(
                        "Report not found or access denied"));
        return mapToResponse(request);
    }

    @Transactional
    public byte[] downloadReport(Long reportId, Long requestedBy) {
        ReportRequest request = reportRequestRepository
                .findByIdAndRequestedBy(reportId, requestedBy)
                .orElseThrow(() -> new ReportException(
                        "Report not found or access denied"));

        if (request.getStatus() != ReportStatus.COMPLETED) {
            throw new ReportException(
                    "Report not ready. Status: " + request.getStatus());
        }

        if (request.getExpiresAt() != null
                && LocalDateTime.now().isAfter(request.getExpiresAt())) {
            throw new ReportException("Report has expired");
        }

        try {
            byte[] bytes = Files.readAllBytes(Paths.get(request.getFilePath()));
            reportRequestRepository.incrementDownloadCount(reportId);
            return bytes;
        } catch (IOException e) {
            throw new ReportException(
                    "Error reading report file: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<ReportRequestResponseDTO> getMyReports(Long requestedBy,
                                                       Pageable pageable) {
        return reportRequestRepository
                .findByRequestedByOrderByRequestedAtDesc(requestedBy, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReportRequestResponseDTO> getAllReports(ReportStatus status,
                                                        String reportType,
                                                        Pageable pageable) {
        return reportRequestRepository
                .findAllWithFilters(status, reportType, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<ReportDefinition> getAvailableReports() {
        return reportDefinitionRepository.findByIsActiveTrue();
    }

    private ReportRequestResponseDTO mapToResponse(ReportRequest r) {
        return ReportRequestResponseDTO.builder()
                .id(r.getId())
                .reportType(r.getReportType())
                .reportName(r.getReportDefinition() != null
                        ? r.getReportDefinition().getReportName() : "")
                .reportFormat(r.getReportFormat())
                .status(r.getStatus())
                .fileName(r.getFileName())
                .fileSizeBytes(r.getFileSizeBytes())
                .downloadCount(r.getDownloadCount())
                .requestedAt(r.getRequestedAt())
                .completedAt(r.getCompletedAt())
                .expiresAt(r.getExpiresAt())
                .errorMessage(r.getErrorMessage())
                .downloadUrl(r.getStatus() == ReportStatus.COMPLETED
                        ? "/reports/" + r.getId() + "/download"
                        : null)
                .build();
    }
}