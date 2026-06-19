package com.dvein.banking_backend.card.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.card.dto.request.SetCardPinRequest;
import com.dvein.banking_backend.card.dto.response.CreditCardResponse;
import com.dvein.banking_backend.card.model.CreditCard;
import com.dvein.banking_backend.card.repository.CreditCardRepository;
import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.common.enums.CardStatus;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.util.CardNumberGenerator;
import com.dvein.banking_backend.common.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final AccountRepository accountRepository;
    private final CardNumberGenerator cardNumberGenerator;
    private final EncryptionUtil encryptionUtil;

    @Transactional
    public CreditCardResponse applyCreditCard(Long accountId, BigDecimal requestedLimit) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        // Generate card details
        String cardNumber = cardNumberGenerator.generateUniqueCardNumber(
                num -> creditCardRepository.existsByCardNumber(num)
        );
        String cvv = cardNumberGenerator.generateCVV();

        CreditCard creditCard = CreditCard.builder()
                .account(account)
                .cardNumber(cardNumber)
                .cardHolderName(account.getCustomer().getFullName())
                .cvv(cvv)
                .expiryDate(cardNumberGenerator.generateExpiryDate())
                .creditLimit(requestedLimit)
                .availableCredit(requestedLimit)
                .status(CardStatus.INACTIVE)
                .build();

        creditCard = creditCardRepository.save(creditCard);

        log.info("Credit card application created for account: {}", accountId);

        return mapToCreditCardResponse(creditCard);
    }

    public List<CreditCardResponse> getAccountCreditCards(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        List<CreditCard> cards = creditCardRepository.findByAccount(account);

        return cards.stream()
                .map(this::mapToCreditCardResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, entityType = "CreditCard", description = "Credit card approved")
    public void approveCreditCard(Long cardId, BigDecimal approvedLimit) {
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit card", "id", cardId));

        card.setApproved(true);
        card.setCreditLimit(approvedLimit);
        card.setAvailableCredit(approvedLimit);
        card.setApprovedAt(LocalDateTime.now());
        card.setBillingDueDate(LocalDate.now().plusMonths(1));
        creditCardRepository.save(card);

        log.info("Credit card approved: {} with limit: {}", cardId, approvedLimit);
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, entityType = "CreditCard", description = "Credit card rejected")
    public void rejectCreditCard(Long cardId, String reason) {
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit card", "id", cardId));

        card.setApproved(false);
        card.setRejectionReason(reason);
        card.setRejectedAt(LocalDateTime.now());
        creditCardRepository.save(card);

        log.info("Credit card rejected: {} - Reason: {}", cardId, reason);
    }

    @Transactional
    public void activateCreditCard(Long cardId) {
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit card", "id", cardId));

        if (!card.isApproved()) {
            throw new InvalidRequestException("Card is not approved");
        }

        if (card.getStatus() != CardStatus.INACTIVE) {
            throw new InvalidRequestException("Card is not in inactive state");
        }

        card.setStatus(CardStatus.ACTIVE);
        card.setActivatedAt(LocalDateTime.now());
        creditCardRepository.save(card);

        log.info("Credit card activated: {}", cardId);
    }

    @Transactional
    public void blockCreditCard(Long cardId, String reason) {
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit card", "id", cardId));

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidRequestException(
                    "Only active cards can be blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        card.setBlockReason(reason);
        card.setBlockedAt(LocalDateTime.now());
        creditCardRepository.save(card);

        log.info("Credit card blocked: {} - Reason: {}", cardId, reason);
    }

    @Transactional
    public void unblockCreditCard(Long cardId) {

        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Credit card",
                                "id",
                                cardId));

        if (card.getStatus() != CardStatus.BLOCKED) {
            throw new InvalidRequestException(
                    "Card is not in blocked state");
        }

        card.setStatus(CardStatus.ACTIVE);
        card.setBlockReason(null);
        card.setBlockedAt(null);

        creditCardRepository.save(card);

        log.info("Credit card unblocked: {}", cardId);
    }

    @Transactional
    public void setCardPin(Long cardId, SetCardPinRequest request) {
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit card", "id", cardId));

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidRequestException(
                    "Card must be active before setting PIN");
        }

        if (!request.getPin().equals(request.getConfirmPin())) {
            throw new InvalidRequestException("PIN and confirm PIN do not match");
        }

        String hashedPin = encryptionUtil.hashPassword(request.getPin());
        card.setPinHash(hashedPin);
        card.setPin(null);
        creditCardRepository.save(card);

        log.info("Card PIN set for credit card: {}", cardId);
    }

    private CreditCardResponse mapToCreditCardResponse(CreditCard card) {
        return CreditCardResponse.builder()
                .cardId(card.getId())
                .maskedCardNumber(cardNumberGenerator.maskCardNumber(card.getCardNumber()))
                .cardHolderName(card.getCardHolderName())
                .expiryDate(card.getExpiryDate())
                .creditLimit(card.getCreditLimit())
                .availableCredit(card.getAvailableCredit())
                .outstandingBalance(card.getOutstandingBalance())
                .interestRate(card.getInterestRate())
                .status(card.getStatus())
                .approved(card.isApproved())
                .rejectionReason(card.getRejectionReason())
                .billingDueDate(card.getBillingDueDate())
                .createdAt(card.getCreatedAt())
                .approvedAt(card.getApprovedAt())
                .activatedAt(card.getActivatedAt())
                .build();
    }
}