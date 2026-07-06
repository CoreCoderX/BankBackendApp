package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.transaction.enums.DisputeStatus;
import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.model.TransactionDispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionDisputeRepository extends JpaRepository<TransactionDispute, Long> {

    List<TransactionDispute> findByTransactionOrderByCreatedAtDesc(Transaction transaction);

    List<TransactionDispute> findByCustomerOrderByCreatedAtDesc(Customer customer);

    Page<TransactionDispute> findByStatus(DisputeStatus status, Pageable pageable);

    @Query("SELECT d FROM TransactionDispute d WHERE d.id = :id " +
            "AND d.customer.user.email = :email")
    Optional<TransactionDispute> findByIdAndUserEmail(@Param("id") Long id,
                                                      @Param("email") String email);
}