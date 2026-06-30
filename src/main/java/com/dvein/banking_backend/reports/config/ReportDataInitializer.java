package com.dvein.banking_backend.reports.config;

import com.dvein.banking_backend.reports.entity.ReportDefinition;
import com.dvein.banking_backend.reports.repository.ReportDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.dvein.banking_backend.reports.entity.ReportCategory;

@Component
@RequiredArgsConstructor
public class ReportDataInitializer implements CommandLineRunner {

    private final ReportDefinitionRepository reportDefinitionRepository;

    @Override
    public void run(String... args) {

        if (reportDefinitionRepository.count() > 0) {
            return;
        }

        // Customer Report
        reportDefinitionRepository.save(
                ReportDefinition.builder()
                        .reportCode("CUSTOMER_REPORT")
                        .reportName("Customer Report")
                        .reportCategory(ReportCategory.CUSTOMER)
                        .description("Customer details report")
                        .requiresDateRange(true)
                        .isActive(true)
                        .build()
        );

        // Account Report
        reportDefinitionRepository.save(
                ReportDefinition.builder()
                        .reportCode("ACCOUNT_REPORT")
                        .reportName("Account Report")
                        .reportCategory(ReportCategory.ACCOUNT)
                        .description("Account summary report")
                        .requiresDateRange(true)
                        .isActive(true)
                        .build()
        );

        // Transaction Report
        reportDefinitionRepository.save(
                ReportDefinition.builder()
                        .reportCode("TRANSACTION_REPORT")
                        .reportName("Transaction Report")
                        .reportCategory(ReportCategory.TRANSACTION)
                        .description("Transaction history report")
                        .requiresDateRange(true)
                        .isActive(true)
                        .build()
        );

        // Audit Report
        reportDefinitionRepository.save(
                ReportDefinition.builder()
                        .reportCode("AUDIT_REPORT")
                        .reportName("Audit Report")
                        .reportCategory(ReportCategory.AUDIT)
                        .description("Audit log report")
                        .requiresDateRange(true)
                        .isActive(true)
                        .build()
        );

        // ✅ NEW: Loan Statement
        reportDefinitionRepository.save(
                ReportDefinition.builder()
                        .reportCode("LOAN_STATEMENT")
                        .reportName("Loan Statement")
                        .reportCategory(ReportCategory.LOAN)
                        .description("Summary of all loans with status and outstanding amount")
                        .requiresDateRange(true)
                        .isActive(true)
                        .build()
        );

        // ✅ NEW: Account Statement
        reportDefinitionRepository.save(
                ReportDefinition.builder()
                        .reportCode("ACCOUNT_STATEMENT")
                        .reportName("Account Statement")
                        .reportCategory(ReportCategory.ACCOUNT)
                        .description("Full account statement with all transactions")
                        .requiresDateRange(true)
                        .isActive(true)
                        .build()
        );

        // ✅ NEW: Financial Summary
        reportDefinitionRepository.save(
                ReportDefinition.builder()
                        .reportCode("FINANCIAL_SUMMARY")
                        .reportName("Financial Summary")
                        .reportCategory(ReportCategory.FINANCIAL)
                        .description("Overall financial summary including deposits, loans, and revenue")
                        .requiresDateRange(true)
                        .isActive(true)
                        .build()
        );

        System.out.println("✅ Report definitions initialized successfully (7 reports).");
    }
}