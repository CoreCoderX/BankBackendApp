package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.enums.TransactionType;
import com.dvein.banking_backend.transaction.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    List<Transaction> findByStatusAndInitiatedAtBetween(
            TransactionStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    // IDOR-safe: fetch transaction only if it belongs to the user's account
    @Query("SELECT t FROM Transaction t WHERE t.id = :txnId " +
            "AND (t.senderAccount.customer.user.email = :email " +
            "OR t.receiverAccount.customer.user.email = :email)")
    Optional<Transaction> findByIdAndUserEmail(@Param("txnId") Long txnId,
                                               @Param("email") String email);

    List<Transaction> findBySenderAccountOrReceiverAccountOrderByInitiatedAtDesc(
            Account senderAccount, Account receiverAccount);

    Page<Transaction> findBySenderAccountOrReceiverAccountOrderByInitiatedAtDesc(
            Account senderAccount, Account receiverAccount, Pageable pageable);

    List<Transaction> findBySenderAccountAndStatusOrderByInitiatedAtDesc(
            Account account, TransactionStatus status);

    List<Transaction> findBySenderAccountAndTransactionTypeOrderByInitiatedAtDesc(
            Account account, TransactionType type);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.senderAccount = :account OR t.receiverAccount = :account) " +
            "AND t.status = :status ORDER BY t.initiatedAt DESC")
    List<Transaction> findByAccountAndStatus(@Param("account") Account account,
                                             @Param("status") TransactionStatus status);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.senderAccount = :account OR t.receiverAccount = :account) " +
            "AND t.initiatedAt BETWEEN :startDate AND :endDate " +
            "ORDER BY t.initiatedAt DESC")
    List<Transaction> findByAccountAndDateRange(@Param("account") Account account,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE t.status = :status " +
            "AND t.initiatedAt < :cutoffTime ORDER BY t.initiatedAt ASC")
    List<Transaction> findFailedTransactionsForRetry(@Param("status") TransactionStatus status,
                                                     @Param("cutoffTime") LocalDateTime cutoffTime);

    List<Transaction> findByStatusAndInitiatedAtBefore(TransactionStatus status,
                                                       LocalDateTime cutoffTime);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE " +
            "t.senderAccount.customer.id = :customerId " +
            "AND t.status = 'COMPLETED' " +
            "AND t.initiatedAt BETWEEN :startTime AND :endTime")
    long countRecentTransactionsByCustomer(@Param("customerId") Long customerId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.senderAccount = :account
        AND t.status = com.dvein.banking_backend.transaction.enums.TransactionStatus.COMPLETED
        AND t.transactionType = :type
        AND t.initiatedAt >= :start
        AND t.initiatedAt < :end
        """)
    BigDecimal sumDailyAmountByAccountAndType(
            @Param("account") Account account,
            @Param("type") TransactionType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE t.status = 'PENDING' " +
            "AND EXISTS (SELECT 1 FROM TransactionApproval ta WHERE ta.transaction = t " +
            "AND ta.requiresApproval = true AND ta.approved = false)")
    Page<Transaction> findPendingApprovals(Pageable pageable);

    @Query("""
            SELECT COUNT(t)
            FROM Transaction t
            WHERE t.initiatedAt >= :start
            AND t.initiatedAt < :end
            AND t.status = com.dvein.banking_backend.transaction.enums.TransactionStatus.COMPLETED
            """)
    long countTodayTransactions(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.initiatedAt >= :start
            AND t.initiatedAt < :end
            AND t.status = com.dvein.banking_backend.transaction.enums.TransactionStatus.COMPLETED
            """)
    BigDecimal sumTodayTransactionAmount(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);
}