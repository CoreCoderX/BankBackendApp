// report/service/ExcelReportService.java
package com.dvein.banking_backend.report.service;

import com.dvein.banking_backend.report.model.ReportRequest;
import com.dvein.banking_backend.report.enums.ReportType;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import com.dvein.banking_backend.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelReportService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public byte[] generateExcel(ReportRequest reportRequest) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Report");

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // Add content based on report type
            int rowIndex = 0;

            switch (reportRequest.getReportType()) {
                case ACCOUNT_STATEMENT:
                    rowIndex = addAccountStatementData(sheet, reportRequest, headerStyle, dataStyle);
                    break;
                case TRANSACTION_HISTORY:
                    rowIndex = addTransactionHistoryData(sheet, reportRequest, headerStyle, dataStyle);
                    break;
                // Add other report types...
                default:
                    addDefaultData(sheet, headerStyle, dataStyle);
            }

            // Auto-size columns
            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating Excel report: {}", reportRequest.getId(), e);
            throw new RuntimeException("Error generating Excel report", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private int addAccountStatementData(Sheet sheet, ReportRequest reportRequest,
                                        CellStyle headerStyle, CellStyle dataStyle) {
        // TODO: Implement account statement Excel generation
        return 0;
    }

    private int addTransactionHistoryData(Sheet sheet, ReportRequest reportRequest,
                                          CellStyle headerStyle, CellStyle dataStyle) {
        // TODO: Implement transaction history Excel generation
        return 0;
    }

    private void addDefaultData(Sheet sheet, CellStyle headerStyle, CellStyle dataStyle) {
        Row headerRow = sheet.createRow(0);
        Cell cell = headerRow.createCell(0);
        cell.setCellValue("Report Data");
        cell.setCellStyle(headerStyle);
    }
}