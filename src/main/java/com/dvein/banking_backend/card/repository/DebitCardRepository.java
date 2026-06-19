package com.dvein.banking_backend.card.repository;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.card.model.DebitCard;
import com.dvein.banking_backend.common.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DebitCardRepository extends JpaRepository<DebitCard, Long> {

    Optional<DebitCard> findByCardNumber(String cardNumber);

    List<DebitCard> findByAccount(Account account);

    List<DebitCard> findByAccountAndStatus(Account account, CardStatus status);

    long countByAccount(Account account);

    boolean existsByCardNumber(String cardNumber);
}