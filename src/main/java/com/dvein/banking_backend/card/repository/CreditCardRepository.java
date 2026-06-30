package com.dvein.banking_backend.card.repository;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.card.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    List<CreditCard> findByAccount(Account account);

    List<CreditCard> findByAccountAndApprovedTrue(Account account);

    boolean existsByAccountAndApprovedFalseAndRejectionReasonIsNull(Account account);

    boolean existsByCardNumber(String cardNumber);

    List<CreditCard> findByApprovedFalseAndRejectionReasonIsNull();

    long countByApprovedTrueAndRejectionReasonIsNull();

    /** Pending applications only — approved=false AND not rejected */
    long countByApprovedFalseAndRejectionReasonIsNull();

    /**
     * IDOR-safe lookup: fetch credit card only if it belongs to the authenticated user.
     * Mirrors the pattern used in DebitCardRepository.
     */
    Optional<CreditCard> findByIdAndAccountCustomerUserEmail(Long cardId, String email);
}