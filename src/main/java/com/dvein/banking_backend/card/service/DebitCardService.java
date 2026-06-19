package com.dvein.banking_backend.card.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.card.dto.request.SetCardPinRequest;
import com.dvein.banking_backend.card.dto.response.DebitCardResponse;
import com.dvein.banking_backend.card.model.DebitCard;
import com.dvein.banking_backend.card.repository.DebitCardRepository;
import com.dvein.banking_backend.common.enums.CardStatus;
import com.dvein.banking_backend.common.exception.DuplicateResourceException;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.util.CardNumberGenerator;
import com.dvein.banking_backend.common.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebitCardService {

    private final DebitCardRepository debitCardRepository;
    private final AccountRepository accountRepository;
    private final CardNumberGenerator cardNumberGenerator;
    private final EncryptionUtil encryptionUtil;

    @Transactional
    public DebitCardResponse generateDebitCard(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        // Check if account already has active debit card
        List<DebitCard> existingCards = debitCardRepository.findByAccountAndStatus(account, CardStatus.ACTIVE);
        if (!existingCards.isEmpty()) {
            throw new DuplicateResourceException("Debit card", "account");
        }

        // Generate card details
        String cardNumber = cardNumberGenerator.generateUniqueCardNumber(
                num -> debitCardRepository.existsByCardNumber(num)
        );
        String cvv = cardNumberGenerator.generateCVV();

        DebitCard debitCard = DebitCard.builder()
                .account(account)
                .cardNumber(cardNumber)
                .cardHolderName(account.getCustomer().getFullName())
                .cvv(cvv)
                .expiryDate(cardNumberGenerator.generateExpiryDate())
                .status(CardStatus.INACTIVE)
                .build();

        debitCard = debitCardRepository.save(debitCard);

        log.info("Debit card generated for account: {}", accountId);

        return mapToDebitCardResponse(debitCard);
    }

    public List<DebitCardResponse> getAccountDebitCards(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        List<DebitCard> cards = debitCardRepository.findByAccount(account);

        return cards.stream()
                .map(this::mapToDebitCardResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void activateDebitCard(Long cardId) {
        DebitCard card = debitCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Debit card", "id", cardId));

        if (card.getStatus() != CardStatus.INACTIVE) {
            throw new InvalidRequestException("Card is not in inactive state");
        }

        card.setStatus(CardStatus.ACTIVE);
        card.setActivatedAt(LocalDateTime.now());
        debitCardRepository.save(card);

        log.info("Debit card activated: {}", cardId);
    }

    @Transactional
    public void blockDebitCard(Long cardId, String reason) {
        DebitCard card = debitCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Debit card", "id", cardId));

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidRequestException(
                    "Only active cards can be blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        card.setBlockReason(reason);
        card.setBlockedAt(LocalDateTime.now());
        debitCardRepository.save(card);

        log.info("Debit card blocked: {} - Reason: {}", cardId, reason);
    }

    @Transactional
    public void unblockDebitCard(Long cardId) {
        DebitCard card = debitCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Debit card", "id", cardId));

        if (card.getStatus() != CardStatus.BLOCKED) {
            throw new InvalidRequestException("Card is not in blocked state");
        }

        card.setStatus(CardStatus.ACTIVE);
        card.setBlockReason(null);
        card.setBlockedAt(null);
        debitCardRepository.save(card);

        log.info("Debit card unblocked: {}", cardId);
    }

    @Transactional
    public void setCardPin(Long cardId, SetCardPinRequest request) {
        DebitCard card = debitCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Debit card", "id", cardId));

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidRequestException(
                    "Card must be active before setting PIN");
        }

        if (!request.getPin().equals(request.getConfirmPin())) {
            throw new InvalidRequestException("PIN and confirm PIN do not match");
        }

        String hashedPin = encryptionUtil.hashPassword(request.getPin());
        card.setPinHash(hashedPin);
        card.setPin(null); // Don't store plain PIN
        debitCardRepository.save(card);

        log.info("Card PIN set for card: {}", cardId);
    }

    @Transactional
    public void toggleInternationalTransaction(Long cardId, boolean enable) {
        DebitCard card = debitCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Debit card", "id", cardId));

        card.setInternationalEnabled(enable);
        debitCardRepository.save(card);

        log.info("International transaction {} for card: {}", enable ? "enabled" : "disabled", cardId);
    }

    private DebitCardResponse mapToDebitCardResponse(DebitCard card) {
        return DebitCardResponse.builder()
                .cardId(card.getId())
                .maskedCardNumber(cardNumberGenerator.maskCardNumber(card.getCardNumber()))
                .cardHolderName(card.getCardHolderName())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .internationalEnabled(card.isInternationalEnabled())
                .onlineTransactionEnabled(card.isOnlineTransactionEnabled())
                .atmWithdrawalEnabled(card.isAtmWithdrawalEnabled())
                .createdAt(card.getCreatedAt())
                .activatedAt(card.getActivatedAt())
                .build();
    }
}