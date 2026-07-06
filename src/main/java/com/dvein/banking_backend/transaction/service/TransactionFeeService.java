package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.transaction.enums.TransactionType;
import com.dvein.banking_backend.transaction.model.TransactionFeeConfig;
import com.dvein.banking_backend.transaction.repository.TransactionFeeConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionFeeService {

    private final TransactionFeeConfigRepository feeConfigRepository;

    public FeeCalculation calculateFee(TransactionType transactionType, BigDecimal amount) {
        TransactionFeeConfig config = feeConfigRepository
                .findByTransactionType(transactionType.name())
                .orElse(createDefaultConfig(transactionType));

        BigDecimal baseFee = config.getBaseFee();
        BigDecimal gstAmount = baseFee.multiply(config.getGstPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalFee = baseFee.add(gstAmount);

        return FeeCalculation.builder()
                .baseFee(baseFee)
                .gst(gstAmount)
                .totalFee(totalFee)
                .build();
    }

    private TransactionFeeConfig createDefaultConfig(TransactionType type) {
        BigDecimal baseFee = BigDecimal.ZERO;

        switch (type) {
            case IMPS:
                baseFee = BigDecimal.valueOf(5.00);
                break;
            case RTGS:
                baseFee = BigDecimal.valueOf(30.00);
                break;
            case NEFT:
            case UPI_TRANSFER:
            case INTERNAL_TRANSFER:
            default:
                baseFee = BigDecimal.ZERO;
                break;
        }

        return TransactionFeeConfig.builder()
                .transactionType(type.name())
                .baseFee(baseFee)
                .gstPercentage(BigDecimal.valueOf(18))
                .active(true)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FeeCalculation {
        private BigDecimal baseFee;
        private BigDecimal gst;
        private BigDecimal totalFee;
    }
}