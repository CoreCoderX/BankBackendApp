package com.dvein.banking_backend.card.repository;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.card.model.CreditCard;
import com.dvein.banking_backend.common.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    Optional<CreditCard> findByCardNumber(String cardNumber);

    List<CreditCard> findByAccount(Account account);

    List<CreditCard> findByAccountAndStatus(Account account, CardStatus status);

    Page<CreditCard> findByApprovedFalseAndStatus(CardStatus status, Pageable pageable);

    long countByAccount(Account account);

    boolean existsByCardNumber(String cardNumber);
}