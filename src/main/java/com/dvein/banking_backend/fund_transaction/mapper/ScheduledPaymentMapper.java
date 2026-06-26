package com.dvein.banking_backend.transaction.mapper;

import com.dvein.banking_backend.transaction.dto.response.ScheduledPaymentResponse;
import com.dvein.banking_backend.transaction.model.ScheduledPayment;
import org.springframework.stereotype.Component;

@Component
public class ScheduledPaymentMapper {

    public ScheduledPaymentResponse toResponse(ScheduledPayment scheduledPayment) {
        return ScheduledPaymentResponse.builder()
                .id(scheduledPayment.getId())
                .beneficiaryNickname(scheduledPayment.getBeneficiary().getNickname())
                .beneficiaryAccountNumber(scheduledPayment.getBeneficiary().getAccountNumber())
                .amount(scheduledPayment.getAmount())
                .frequency(scheduledPayment.getFrequency())
                .nextExecutionDate(scheduledPayment.getNextExecutionDate())
                .status(scheduledPayment.getStatus())
                .remarks(scheduledPayment.getRemarks())
                .createdAt(scheduledPayment.getCreatedAt())
                .build();
    }
}