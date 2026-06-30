package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.model.UpiTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UpiTransactionRepository extends JpaRepository<UpiTransaction, Long> {

    Optional<UpiTransaction> findByTransaction(Transaction transaction);

    List<UpiTransaction> findBySenderUpiIdOrReceiverUpiIdOrderByCreatedAtDesc(
            String senderUpiId, String receiverUpiId);

    List<UpiTransaction> findBySenderUpiIdOrderByCreatedAtDesc(String senderUpiId);
}