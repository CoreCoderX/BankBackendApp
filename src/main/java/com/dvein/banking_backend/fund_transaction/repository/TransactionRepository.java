package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    Page<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.status = :status ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") TransactionStatus status,
            Pageable pageable
    );

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.transactionType = :type ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdAndTransactionType(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            Pageable pageable
    );

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.createdAt BETWEEN :startDate AND :endDate AND t.status = :status ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdAndDateRangeAndStatus(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") TransactionStatus status,
            Pageable pageable
    );

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.createdAt BETWEEN :startDate AND :endDate AND t.transactionType = :type ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdAndDateRangeAndType(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("type") TransactionType type,
            Pageable pageable
    );

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.createdAt BETWEEN :startDate AND :endDate AND t.status = :status AND t.transactionType = :type ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdAndDateRangeAndStatusAndType(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") TransactionStatus status,
            @Param("type") TransactionType type,
            Pageable pageable
    );

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId AND t.createdAt > :since AND t.status = 'SUCCESS'")
    Long countRecentSuccessfulTransactions(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since
    );

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.senderAccount.id = :accountId AND t.createdAt BETWEEN :startDate AND :endDate AND t.status = 'SUCCESS'")
    BigDecimal sumTransactionsByAccountAndDateRange(
            @Param("userId") Long userId,
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<Transaction> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND (:transactionId IS NULL OR t.transactionId LIKE %:transactionId%) " +
            "AND (:accountNumber IS NULL OR t.senderAccount.accountNumber = :accountNumber OR t.receiverAccount.accountNumber = :accountNumber) " +
            "AND (:minAmount IS NULL OR t.amount >= :minAmount) " +
            "AND (:maxAmount IS NULL OR t.amount <= :maxAmount) " +
            "AND (:date IS NULL OR DATE(t.createdAt) = :date) " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> searchTransactions(
            @Param("userId") Long userId,
            @Param("transactionId") String transactionId,
            @Param("accountNumber") String accountNumber,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("date") LocalDate date,
            Pageable pageable
    );

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND (t.senderAccount.id = :accountId OR t.receiverAccount.id = :accountId) ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdAndAccountId(
            @Param("userId") Long userId,
            @Param("accountId") Long accountId,
            Pageable pageable
    );
}