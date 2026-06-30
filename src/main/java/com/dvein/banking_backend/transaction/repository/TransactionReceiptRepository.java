package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.model.TransactionReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionReceiptRepository extends JpaRepository<TransactionReceipt, Long> {

    Optional<TransactionReceipt> findByTransaction(Transaction transaction);

    Optional<TransactionReceipt> findByReceiptNumber(String receiptNumber);

    boolean existsByTransaction(Transaction transaction);
}