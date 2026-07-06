package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.transaction.model.TransactionLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionLimitRepository extends JpaRepository<TransactionLimit, Long> {

    Optional<TransactionLimit> findByCustomer(Customer customer);

    Optional<TransactionLimit> findByCustomerId(Long customerId);

    boolean existsByCustomer(Customer customer);
}