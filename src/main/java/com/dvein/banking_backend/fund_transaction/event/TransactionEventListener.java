package com.dvein.banking_backend.transaction.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {

    @Async
    @EventListener
    public void handleTransactionEvent(TransactionEvent event) {
        log.info("Transaction event received: {} for transaction: {}",
                event.getEventType(),
                event.getTransaction().getTransactionId()
        );

        // Additional processing
        // - Analytics
        // - Audit logging
        // - External system notifications
        // - Fraud analysis
    }
}