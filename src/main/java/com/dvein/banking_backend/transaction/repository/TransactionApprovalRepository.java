package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.model.TransactionApproval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionApprovalRepository extends JpaRepository<TransactionApproval, Long> {

    Optional<TransactionApproval> findByTransaction(Transaction transaction);

    List<TransactionApproval> findByRequiresApprovalTrueAndApprovedFalse();

    Page<TransactionApproval> findByRequiresApprovalTrueAndApprovedFalse(Pageable pageable);

    boolean existsByTransaction(Transaction transaction);
}