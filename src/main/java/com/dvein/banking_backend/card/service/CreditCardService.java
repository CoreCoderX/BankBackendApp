package com.dvein.banking_backend.card.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.card.dto.request.ApplyCreditCardRequest;
import com.dvein.banking_backend.card.dto.request.BlockCardRequest;
import com.dvein.banking_backend.card.dto.request.SetCardPinRequest;
import com.dvein.banking_backend.card.dto.response.CreditCardResponse;
import com.dvein.banking_backend.card.model.CreditCard;
import com.dvein.banking_backend.card.repository.CreditCardRepository;
import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.enums.AuditAction;
import com.dvein.banking_backend.common.enums.CardStatus;
import com.dvein.banking_backend.common.exception.CustomException;
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
    public CreditCardResponse applyCreditCard(Long accountId,
                                              String email,
                                              ApplyCreditCardRequest request) {
        Account account = accountRepository.findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        // Check for existing pending applications
        boolean hasPendingApplication = creditCardRepository
                .existsByAccountAndApprovedFalseAndRejectionReasonIsNull(account);

        if (hasPendingApplication) {
            throw new InvalidRequestException("You already have a pending credit card application");
        }

        // Check for existing approved cards with enough limit
        List<CreditCard> existingCards = creditCardRepository.findByAccountAndApprovedTrue(account);
        if (existingCards.size() >= 3) { // Max 3 credit cards per account
            throw new InvalidRequestException("Maximum credit card limit reached for this account");
        }

        // Create application WITHOUT card number
        CreditCard creditCard = CreditCard.builder()
                .account(account)
                .cardNumber(null) // Will be generated on approval
                .cardHolderName(request.getCardHolderName())
                .cvv(null) // Will be generated on approval
                .expiryDate(null) // Will be set on approval
                .creditLimit(request.getRequestedCreditLimit())
                .availableCredit(BigDecimal.ZERO)
                .outstandingBalance(BigDecimal.ZERO)
                .interestRate(BigDecimal.ZERO) // Will be set on approval
                .status(CardStatus.INACTIVE)
                .approved(false)
                .build();

        creditCard = creditCardRepository.save(creditCard);

        log.info("Credit card application submitted - Account: {} - Requested Limit: {}",
                accountId, request.getRequestedCreditLimit());

        return mapToCreditCardResponse(creditCard);
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, entityType = "CreditCard", description = "Credit card approved")
    public void approveCreditCard(Long cardId, BigDecimal approvedLimit, BigDecimal interestRate, String approvedBy) {
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("CreditCard", "id", cardId));

        if (card.isApproved()) {
            throw new InvalidRequestException("Credit card is already approved");
        }

        if (card.getRejectionReason() != null) {
            throw new InvalidRequestException("Credit card was already rejected");
        }

        if (approvedLimit == null ||
                approvedLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException(
                    "Approved limit must be greater than zero");
        }

        // NOW GENERATE CARD NUMBER AND DETAILS
        String cardNumber = cardNumberGenerator.generateUniqueCardNumber(
                num -> creditCardRepository.existsByCardNumber(num)
        );
        String cvv = cardNumberGenerator.generateCVV();
        LocalDate expiryDate = cardNumberGenerator.generateExpiryDate();

        // Update card with approval details
        card.setCardNumber(cardNumber);
        card.setCvv(cvv);
        card.setExpiryDate(expiryDate);
        card.setCreditLimit(approvedLimit);
        card.setAvailableCredit(approvedLimit);
        card.setInterestRate(interestRate != null ? interestRate : BigDecimal.valueOf(18.5));
        card.setApproved(true);
        card.setApprovedAt(LocalDateTime.now());
        card.setStatus(CardStatus.INACTIVE); // Awaiting customer activation
        card.setBillingDueDate(LocalDate.now().plusMonths(1));

        creditCardRepository.save(card);

        log.info("Credit card APPROVED and GENERATED - Card ID: {} - Approved By: {} - Limit: {}",
                cardId, approvedBy, approvedLimit);
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, entityType = "CreditCard", description = "Credit card rejected")
    public void rejectCreditCard(Long cardId, String reason, String rejectedBy) {
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("CreditCard", "id", cardId));

        if (card.isApproved()) {
            throw new InvalidRequestException("Cannot reject an approved credit card");
        }

        if (card.getRejectionReason() != null) {
            throw new InvalidRequestException("Credit card was already rejected");
        }

        card.setRejectionReason(reason);
        card.setRejectedAt(LocalDateTime.now());
        card.setApproved(false);

        creditCardRepository.save(card);

        log.info("Credit card REJECTED - Card ID: {} - Rejected By: {} - Reason: {}",
                cardId, rejectedBy, reason);
    }

    @Transactional
    public void activateCreditCard(Long cardId, String email) {
        // FIX IDOR: use scoped query — only fetches if the card belongs to this user's account
        CreditCard card = creditCardRepository.findByIdAndAccountCustomerUserEmail(cardId, email)
                .orElseThrow(() -> new ResourceNotFoundException("CreditCard", "id", cardId));

        if (!card.isApproved()) {
            throw new InvalidRequestException("Card is not approved yet. Please wait for admin approval.");
        }

        if (card.getRejectionReason() != null) {
            throw new InvalidRequestException("Cannot activate a rejected card");
        }

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new InvalidRequestException("Card is already active");
        }

        if (card.isExpired()) {
            throw new InvalidRequestException("Card has expired");
        }

        card.setStatus(CardStatus.ACTIVE);
        card.setActivatedAt(LocalDateTime.now());
        creditCardRepository.save(card);

        log.info("Credit card ACTIVATED by customer - Card ID: {} - Account: {}", cardId, email);
    }

    @Transactional
    public void blockCreditCard(
            Long cardId,
            String email,
            BlockCardRequest request) {

        // FIX IDOR: use scoped query
        CreditCard card = creditCardRepository.findByIdAndAccountCustomerUserEmail(cardId, email)
                .orElseThrow(() -> new ResourceNotFoundException("CreditCard", "id", cardId));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new InvalidRequestException("Card is already blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        card.setBlockReason(request.getReason());
        card.setBlockedAt(LocalDateTime.now());
        creditCardRepository.save(card);

        log.info("Credit card BLOCKED - Card ID: {} - Reason: {}", cardId, request.getReason());
    }

    @Transactional
    public void unblockCreditCard(Long cardId, String email) {
        // FIX IDOR: use scoped query
        CreditCard card = creditCardRepository.findByIdAndAccountCustomerUserEmail(cardId, email)
                .orElseThrow(() -> new ResourceNotFoundException("CreditCard", "id", cardId));

        if (card.getStatus() != CardStatus.BLOCKED) {
            throw new InvalidRequestException("Card is not blocked");
        }

        card.setStatus(CardStatus.ACTIVE);
        card.setBlockReason(null);
        card.setBlockedAt(null);
        creditCardRepository.save(card);

        log.info("Credit card UNBLOCKED - Card ID: {}", cardId);
    }

    @Transactional
    public void setCardPin(
            Long cardId,
            String email,
            SetCardPinRequest request) {
        // FIX IDOR: use scoped query
        CreditCard card = creditCardRepository.findByIdAndAccountCustomerUserEmail(cardId, email)
                .orElseThrow(() -> new ResourceNotFoundException("CreditCard", "id", cardId));

        if (!card.isApproved()) {
            throw new InvalidRequestException("Cannot set PIN for unapproved card");
        }

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidRequestException("Card must be activated before setting PIN");
        }

        if (!request.getPin().equals(request.getConfirmPin())) {
            throw new InvalidRequestException("PIN and confirm PIN do not match");
        }

        String pinHash = encryptionUtil.hashPassword(request.getPin());
        card.setPinHash(pinHash);
        card.setPin(null); // Never store plain PIN
        creditCardRepository.save(card);

        log.info("PIN set for credit card - Card ID: {}", cardId);
    }

    public List<CreditCardResponse> getAccountCreditCards(Long accountId, String email) {
        Account account = accountRepository
                .findByIdAndCustomerUserEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        List<CreditCard> cards = creditCardRepository.findByAccount(account);

        return cards.stream()
                .map(this::mapToCreditCardResponse)
                .collect(Collectors.toList());
    }

    public List<CreditCardResponse> getPendingCreditCardApplications() {
        List<CreditCard> pendingCards = creditCardRepository
                .findByApprovedFalseAndRejectionReasonIsNull();

        return pendingCards.stream()
                .map(this::mapToCreditCardResponse)
                .collect(Collectors.toList());
    }

    private CreditCardResponse mapToCreditCardResponse(CreditCard card) {
        String maskedCardNumber = null;
        LocalDate expiryDate = null;

        if (card.isApproved() && card.getCardNumber() != null) {
            maskedCardNumber = cardNumberGenerator.maskCardNumber(card.getCardNumber());
            expiryDate = card.getExpiryDate();
        }

        return CreditCardResponse.builder()
                .cardId(card.getId())
                .maskedCardNumber(maskedCardNumber)
                .cardHolderName(card.getCardHolderName())
                .expiryDate(expiryDate)
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

    // Removed: approveCreditCardApplication — was dead code (incomplete duplicate of approveCreditCard).
    // All admin card approvals should go through approveCreditCard(cardId, approvedLimit, interestRate, approvedBy).
}