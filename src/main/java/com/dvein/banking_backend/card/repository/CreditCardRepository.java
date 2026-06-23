package com.dvein.banking_backend.card.repository;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.card.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    List<CreditCard> findByAccount(Account account);

    List<CreditCard> findByAccountAndApprovedTrue(Account account);

    boolean existsByAccountAndApprovedFalseAndRejectionReasonIsNull(Account account);

    boolean existsByCardNumber(String cardNumber);

    List<CreditCard> findByApprovedFalseAndRejectionReasonIsNull();

    long countByApprovedTrueAndRejectionReasonIsNull();
}