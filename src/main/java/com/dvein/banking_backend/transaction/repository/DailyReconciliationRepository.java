package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.DailyReconciliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyReconciliationRepository extends JpaRepository<DailyReconciliation, Long> {

    Optional<DailyReconciliation> findByReconciliationDate(LocalDate date);

    boolean existsByReconciliationDate(LocalDate date);
}