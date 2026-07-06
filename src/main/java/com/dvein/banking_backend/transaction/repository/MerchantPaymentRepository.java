package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.Merchant;
import com.dvein.banking_backend.transaction.model.MerchantPayment;
import com.dvein.banking_backend.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantPaymentRepository extends JpaRepository<MerchantPayment, Long> {

    Optional<MerchantPayment> findByTransaction(Transaction transaction);

    List<MerchantPayment> findByMerchantOrderByCreatedAtDesc(Merchant merchant);
}