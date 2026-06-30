package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.TransactionFeeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionFeeConfigRepository extends JpaRepository<TransactionFeeConfig, Long> {

    Optional<TransactionFeeConfig> findByTransactionType(String transactionType);

    List<TransactionFeeConfig> findByActiveTrue();

    boolean existsByTransactionType(String transactionType);
}