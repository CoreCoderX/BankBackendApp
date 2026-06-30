package com.dvein.banking_backend.reports.service;

import com.dvein.banking_backend.reports.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * This service executes raw/native SQL queries against existing tables
 * (transactions, accounts, customers, loans) created by other team members.
 * Reports module does NOT duplicate data — it only READS from existing tables.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportDataQueryService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Fetch transaction summary data for the given date range.
     * Reads from the 'transactions' table (owned by Ajay's module).
     *
     * ✅ FIXED: Using correct column names from Transaction.java:
     *    - transaction_type (not type)
     *    - We categorize by transaction_type values (DEPOSIT, WITHDRAWAL, TRANSFER, etc.)
     */
    public TransactionReportDataDTO getTransactionSummary(LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching transaction summary from {} to {}", fromDate, toDate);

        String sql = """
                SELECT
                    COUNT(*) AS total_transactions,
                    COALESCE(SUM(CASE WHEN transaction_type = 'DEPOSIT' THEN amount ELSE 0 END), 0) AS total_credit,
                    COALESCE(SUM(CASE WHEN transaction_type = 'WITHDRAWAL' THEN amount ELSE 0 END), 0) AS total_debit,
                    COUNT(CASE WHEN transaction_type = 'DEPOSIT' THEN 1 END) AS credit_count,
                    COUNT(CASE WHEN transaction_type = 'WITHDRAWAL' THEN 1 END) AS debit_count,
                    COALESCE(AVG(amount), 0) AS avg_amount,
                    COALESCE(MAX(amount), 0) AS max_amount,
                    COALESCE(MIN(amount), 0) AS min_amount
                FROM transactions
                WHERE DATE(initiated_at) BETWEEN ? AND ?
                AND status = 'COMPLETED'
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            BigDecimal totalCredit = rs.getBigDecimal("total_credit");
            BigDecimal totalDebit  = rs.getBigDecimal("total_debit");

            return TransactionReportDataDTO.builder()
                    .reportDate(fromDate)
                    .totalTransactions(rs.getLong("total_transactions"))
                    .totalCreditAmount(totalCredit)
                    .totalDebitAmount(totalDebit)
                    .netAmount(totalCredit.subtract(totalDebit))
                    .totalCreditCount(rs.getLong("credit_count"))
                    .totalDebitCount(rs.getLong("debit_count"))
                    .averageTransactionAmount(rs.getBigDecimal("avg_amount"))
                    .highestTransaction(rs.getBigDecimal("max_amount"))
                    .lowestTransaction(rs.getBigDecimal("min_amount"))
                    .build();
        }, fromDate, toDate);
    }

    /**
     * Fetch daily transaction breakdown (for trend charts in reports).
     */
    public List<Map<String, Object>> getDailyTransactionBreakdown(LocalDate fromDate, LocalDate toDate) {
        String sql = """
                SELECT
                    DATE(initiated_at) AS txn_date,
                    COUNT(*) AS txn_count,
                    SUM(amount) AS total_amount,
                    transaction_type
                FROM transactions
                WHERE DATE(initiated_at) BETWEEN ? AND ?
                AND status = 'COMPLETED'
                GROUP BY DATE(initiated_at), transaction_type
                ORDER BY txn_date ASC
                """;

        return jdbcTemplate.queryForList(sql, fromDate, toDate);
    }

    /**
     * Customer growth report data.
     * Reads from 'customers' table (owned by Siva's module).
     */
    public CustomerReportDataDTO getCustomerReportData(LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching customer data for report from {} to {}", fromDate, toDate);

        String totalSql    = "SELECT COUNT(*) FROM customers";
        String newSql      = "SELECT COUNT(*) FROM customers WHERE DATE(created_at) BETWEEN ? AND ?";
        String activeSql   = "SELECT COUNT(*) FROM customers WHERE status = 'ACTIVE'";
        String inactiveSql = "SELECT COUNT(*) FROM customers WHERE status = 'INACTIVE'";
        String kycSql      = """
                SELECT kyc_status, COUNT(*) as cnt 
                FROM customers 
                GROUP BY kyc_status
                """;

        Long total    = jdbcTemplate.queryForObject(totalSql, Long.class);
        Long newCust  = jdbcTemplate.queryForObject(newSql, Long.class, fromDate, toDate);
        Long active   = jdbcTemplate.queryForObject(activeSql, Long.class);
        Long inactive = jdbcTemplate.queryForObject(inactiveSql, Long.class);

        List<Map<String, Object>> kycData = jdbcTemplate.queryForList(kycSql);

        Long kycPending  = extractKycCount(kycData, "PENDING");
        Long kycApproved = extractKycCount(kycData, "APPROVED");
        Long kycRejected = extractKycCount(kycData, "REJECTED");

        return CustomerReportDataDTO.builder()
                .totalCustomers(total)
                .newCustomersInPeriod(newCust)
                .activeCustomers(active)
                .inactiveCustomers(inactive)
                .kycPendingCustomers(kycPending)
                .kycApprovedCustomers(kycApproved)
                .kycRejectedCustomers(kycRejected)
                .reportFromDate(fromDate)
                .reportToDate(toDate)
                .build();
    }

    /**
     * Loan summary report data.
     * Reads from 'loans' table (owned by Yashwanth's module).
     *
     * ✅ FIXED: Using correct column names from Loan.java:
     *    - principal_amount (not loan_amount)
     *    - remaining_principal (not outstanding_amount)
     *    - amount_paid (not repaid_amount)
     *    - total_interest (not interest_earned)
     *    - disbursed_date (timestamp column)
     */
    public LoanReportDataDTO getLoanReportData(LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching loan data for report from {} to {}", fromDate, toDate);

        String sql = """
                SELECT
                    COUNT(*) AS total_loans,
                    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) AS active_loans,
                    COUNT(CASE WHEN status = 'CLOSED' THEN 1 END) AS closed_loans,
                    COUNT(CASE WHEN status = 'DEFAULTED' THEN 1 END) AS defaulted_loans,
                    COUNT(CASE WHEN status = 'PENDING_APPROVAL' THEN 1 END) AS pending_loans,
                    COALESCE(SUM(principal_amount), 0) AS total_disbursed,
                    COALESCE(SUM(remaining_principal), 0) AS total_outstanding,
                    COALESCE(SUM(amount_paid), 0) AS total_repaid,
                    COALESCE(SUM(total_interest), 0) AS total_interest
                FROM loans
                WHERE (disbursed_date IS NULL OR DATE(disbursed_date) BETWEEN ? AND ?)
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            long totalLoans    = rs.getLong("total_loans");
            long defaultedLoans = rs.getLong("defaulted_loans");
            double defaultRate  = totalLoans > 0
                    ? ((double) defaultedLoans / totalLoans) * 100
                    : 0.0;

            return LoanReportDataDTO.builder()
                    .totalLoans(totalLoans)
                    .activeLoans(rs.getLong("active_loans"))
                    .closedLoans(rs.getLong("closed_loans"))
                    .defaultedLoans(defaultedLoans)
                    .pendingApprovalLoans(rs.getLong("pending_loans"))
                    .totalDisbursedAmount(rs.getBigDecimal("total_disbursed"))
                    .totalOutstandingAmount(rs.getBigDecimal("total_outstanding"))
                    .totalRepaidAmount(rs.getBigDecimal("total_repaid"))
                    .totalInterestEarned(rs.getBigDecimal("total_interest"))
                    .defaultRate(Math.round(defaultRate * 100.0) / 100.0)
                    .build();
        }, fromDate, toDate);
    }

    /**
     * Account statement data for a specific account.
     * Reads from 'transactions' table filtered by account_id.
     *
     * ✅ FIXED: Using correct column names from Transaction.java:
     *    - initiated_at (not transaction_date)
     *    - transaction_type (not transaction_type - same)
     *    - sender_balance_after (not balance_after)
     */
    public List<Map<String, Object>> getAccountStatementData(Long accountId,
                                                             LocalDate fromDate,
                                                             LocalDate toDate) {
        String sql = """
            SELECT
                t.id,
                t.initiated_at AS transaction_date,
                t.transaction_type,
                CASE 
                    WHEN t.sender_account_id = ? THEN -t.amount
                    ELSE t.amount 
                END AS amount,
                CASE 
                    WHEN t.sender_account_id = ? THEN t.sender_balance_after
                    ELSE t.receiver_balance_after
                END AS balance_after,
                t.description,
                t.reference_number,
                t.status,
                CASE 
                    WHEN t.sender_account_id = ? THEN 'DEBIT'
                    ELSE 'CREDIT'
                END AS transaction_direction
            FROM transactions t
            WHERE (t.sender_account_id = ? OR t.receiver_account_id = ?)
            AND DATE(t.initiated_at) BETWEEN ? AND ?
            AND t.status = 'COMPLETED'
            ORDER BY t.initiated_at DESC
            """;

        return jdbcTemplate.queryForList(sql,
                accountId, accountId, accountId, accountId, accountId,
                fromDate, toDate);
    }

    /**
     * Financial summary combining multiple modules' data.
     *
     * ✅ FIXED: Using correct column names from Transaction.java:
     *    - initiated_at (not transaction_date)
     *    - transaction_type for categorization
     */
    public FinancialSummaryReportDTO getFinancialSummary(LocalDate fromDate, LocalDate toDate) {
        log.info("Generating financial summary report from {} to {}", fromDate, toDate);

        // Transaction data
        String txnSql = """
                SELECT
                    COUNT(*) AS total_count,
                    COALESCE(SUM(CASE WHEN transaction_type = 'DEPOSIT' THEN amount ELSE 0 END), 0) AS deposits,
                    COALESCE(SUM(CASE WHEN transaction_type = 'WITHDRAWAL' THEN amount ELSE 0 END), 0) AS withdrawals,
                    COALESCE(SUM(CASE WHEN transaction_type = 'TRANSFER' THEN amount ELSE 0 END), 0) AS transfers
                FROM transactions
                WHERE DATE(initiated_at) BETWEEN ? AND ?
                AND status = 'COMPLETED'
                """;

        Map<String, Object> txnData = jdbcTemplate.queryForMap(txnSql, fromDate, toDate);

        // Loan data — ✅ FIXED column names
        String loanSql = """
                SELECT
                    COALESCE(SUM(principal_amount), 0) AS total_disbursed,
                    COALESCE(SUM(total_interest), 0) AS total_interest
                FROM loans
                WHERE (disbursed_date IS NULL OR DATE(disbursed_date) BETWEEN ? AND ?)
                """;

        Map<String, Object> loanData = jdbcTemplate.queryForMap(loanSql, fromDate, toDate);

        // Account data
        String accSql = """
                SELECT
                    COUNT(CASE WHEN DATE(created_at) BETWEEN ? AND ? THEN 1 END) AS opened,
                    COUNT(CASE WHEN DATE(closed_at) BETWEEN ? AND ? THEN 1 END) AS closed
                FROM accounts
                """;

        Map<String, Object> accData = jdbcTemplate.queryForMap(accSql,
                fromDate, toDate, fromDate, toDate);

        BigDecimal deposits    = toBigDecimal(txnData.get("deposits"));
        BigDecimal withdrawals = toBigDecimal(txnData.get("withdrawals"));
        BigDecimal interest    = toBigDecimal(loanData.get("total_interest"));

        return FinancialSummaryReportDTO.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .totalDeposits(deposits)
                .totalWithdrawals(withdrawals)
                .totalTransferAmount(toBigDecimal(txnData.get("transfers")))
                .totalLoanDisbursed(toBigDecimal(loanData.get("total_disbursed")))
                .totalInterestEarned(interest)
                .netRevenue(deposits.subtract(withdrawals).add(interest))
                .totalAccountsOpened(toLong(accData.get("opened")))
                .totalAccountsClosed(toLong(accData.get("closed")))
                .totalTransactionCount(toLong(txnData.get("total_count")))
                .build();
    }

    // ─── Private Helper Methods ───────────────────────────────────────────────

    private Long extractKycCount(List<Map<String, Object>> kycData, String status) {
        return kycData.stream()
                .filter(row -> status.equalsIgnoreCase(String.valueOf(row.get("kyc_status"))))
                .mapToLong(row -> ((Number) row.get("cnt")).longValue())
                .findFirst()
                .orElse(0L);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        return new BigDecimal(value.toString());
    }

    private Long toLong(Object value) {
        if (value == null) return 0L;
        return ((Number) value).longValue();
    }
}