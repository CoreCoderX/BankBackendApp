package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.transaction.model.ScheduledPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduledPaymentRepository extends JpaRepository<ScheduledPayment, Long> {

    List<ScheduledPayment> findByCustomerOrderByCreatedAtDesc(Customer customer);

    List<ScheduledPayment> findBySenderAccountOrderByCreatedAtDesc(Account account);

    @Query("SELECT sp FROM ScheduledPayment sp WHERE " +
            "sp.active = true AND sp.paused = false " +
            "AND sp.nextExecutionDate <= :date")
    List<ScheduledPayment> findDuePayments(@Param("date") LocalDate date);

    @Query("SELECT sp FROM ScheduledPayment sp WHERE sp.id = :id " +
            "AND sp.customer.user.email = :email")
    Optional<ScheduledPayment> findByIdAndUserEmail(@Param("id") Long id,
                                                    @Param("email") String email);
}