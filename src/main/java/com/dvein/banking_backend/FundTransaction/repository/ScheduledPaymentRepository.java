package com.dvein.banking_backend.FundTransaction.repository;

import com.dvein.banking_backend.FundTransaction.model.ScheduledPayment;
import com.dvein.banking_backend.FundTransaction.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduledPaymentRepository extends JpaRepository<ScheduledPayment, Long> {

    List<ScheduledPayment> findByUserId(Long userId);

    Optional<ScheduledPayment> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT sp FROM ScheduledPayment sp WHERE sp.nextExecutionDate <= :date AND sp.status = :status")
    List<ScheduledPayment> findDuePayments(LocalDate date, TransactionStatus status);
}