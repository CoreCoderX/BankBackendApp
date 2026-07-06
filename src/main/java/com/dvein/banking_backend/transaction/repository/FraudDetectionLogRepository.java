package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.transaction.enums.FraudRiskLevel;
import com.dvein.banking_backend.transaction.model.FraudDetectionLog;
import com.dvein.banking_backend.transaction.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FraudDetectionLogRepository extends JpaRepository<FraudDetectionLog, Long> {

    List<FraudDetectionLog> findByTransactionOrderByCreatedAtDesc(Transaction transaction);

    List<FraudDetectionLog> findByCustomerOrderByCreatedAtDesc(Customer customer);

    Page<FraudDetectionLog> findByRiskLevel(FraudRiskLevel riskLevel, Pageable pageable);

    Page<FraudDetectionLog> findByRiskLevelOrderByCreatedAtDesc(FraudRiskLevel riskLevel, Pageable pageable);

    long countByCustomerAndCreatedAtAfter(Customer customer, LocalDateTime after);
}