package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.BillPayment;
import com.dvein.banking_backend.transaction.model.Biller;
import com.dvein.banking_backend.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillPaymentRepository extends JpaRepository<BillPayment, Long> {

    Optional<BillPayment> findByTransaction(Transaction transaction);

    List<BillPayment> findByBillerOrderByCreatedAtDesc(Biller biller);

    List<BillPayment> findByBillCategoryOrderByCreatedAtDesc(String category);
}