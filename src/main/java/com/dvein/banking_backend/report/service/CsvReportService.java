// report/service/CsvReportService.java
package com.dvein.banking_backend.report.service;

import com.dvein.banking_backend.report.model.ReportRequest;
import com.dvein.banking_backend.report.enums.ReportType;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import com.dvein.banking_backend.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.StringWriter;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvReportService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public byte[] generateCsv(ReportRequest reportRequest) throws IOException {
        try (StringWriter sw = new StringWriter();
             CSVPrinter csvPrinter = new CSVPrinter(sw, CSVFormat.DEFAULT)) {

            switch (reportRequest.getReportType()) {
                case ACCOUNT_STATEMENT:
                    addAccountStatementData(csvPrinter, reportRequest);
                    break;
                case TRANSACTION_HISTORY:
                    addTransactionHistoryData(csvPrinter, reportRequest);
                    break;
                // Add other report types...
                default:
                    addDefaultData(csvPrinter);
            }

            csvPrinter.flush();
            return sw.toString().getBytes();

        } catch (Exception e) {
            log.error("Error generating CSV report: {}", reportRequest.getId(), e);
            throw new RuntimeException("Error generating CSV report", e);
        }
    }

    private void addAccountStatementData(CSVPrinter csvPrinter, ReportRequest reportRequest) throws IOException {
        // TODO: Implement account statement CSV generation
    }

    private void addTransactionHistoryData(CSVPrinter csvPrinter, ReportRequest reportRequest) throws IOException {
        // TODO: Implement transaction history CSV generation
    }

    private void addDefaultData(CSVPrinter csvPrinter) throws IOException {
        csvPrinter.printRecord("Report Data");
    }
}