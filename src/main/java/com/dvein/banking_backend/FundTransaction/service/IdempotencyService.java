package com.dvein.banking_backend.FundTransaction.service;

import com.dvein.banking_backend.FundTransaction.enums.TransactionStatus;
import com.dvein.banking_backend.FundTransaction.exception.DuplicateTransactionException;
import com.dvein.banking_backend.FundTransaction.model.IdempotencyKey;
import com.dvein.banking_backend.FundTransaction.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public void checkIdempotency(String key) {
        if (idempotencyKeyRepository.existsByIdempotencyKey(key)) {
            log.warn("Duplicate transaction attempt with idempotency key: {}", key);
            throw new DuplicateTransactionException("This transaction has already been processed");
        }
    }

    @Transactional
    public void saveIdempotency(String key, TransactionStatus status, String transactionId) {
        IdempotencyKey idempotencyKey = IdempotencyKey.builder()
                .idempotencyKey(key)
                .status(status)
                .transactionId(transactionId)
                .build();

        idempotencyKeyRepository.save(idempotencyKey);
        log.info("Saved idempotency key: {} for transaction: {}", key, transactionId);
    }
}