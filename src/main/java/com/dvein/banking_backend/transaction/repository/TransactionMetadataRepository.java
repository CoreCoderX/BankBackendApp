package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.model.TransactionMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionMetadataRepository extends JpaRepository<TransactionMetadata, Long> {

    List<TransactionMetadata> findByTransaction(Transaction transaction);

    Optional<TransactionMetadata> findByTransactionAndMetaKey(Transaction transaction, String metaKey);
}