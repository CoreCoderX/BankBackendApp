package com.dvein.banking_backend.FundTransaction.event;

import com.dvein.banking_backend.FundTransaction.model.Transaction;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TransactionEvent extends ApplicationEvent {

    private final Transaction transaction;
    private final String eventType;

    public TransactionEvent(Object source, Transaction transaction, String eventType) {
        super(source);
        this.transaction = transaction;
        this.eventType = eventType;
    }
}