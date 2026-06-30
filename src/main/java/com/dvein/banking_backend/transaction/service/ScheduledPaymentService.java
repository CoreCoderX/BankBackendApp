package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Beneficiary;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.account.repository.BeneficiaryRepository;
import com.dvein.banking_backend.account.repository.CustomerRepository;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.transaction.dto.request.SchedulePaymentRequest;
import com.dvein.banking_backend.transaction.dto.response.ScheduledPaymentResponse;
import com.dvein.banking_backend.transaction.enums.PaymentMethod;
import com.dvein.banking_backend.transaction.enums.ScheduleFrequency;
import com.dvein.banking_backend.transaction.enums.TransactionType;
import com.dvein.banking_backend.transaction.model.ScheduledPayment;
import com.dvein.banking_backend.transaction.repository.ScheduledPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledPaymentService {

    private final ScheduledPaymentRepository scheduledPaymentRepository;
    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public ScheduledPaymentResponse createScheduledPayment(SchedulePaymentRequest request, String email) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        Account senderAccount = accountRepository.findByIdAndCustomerUserEmail(request.getSenderAccountId(), email)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", request.getSenderAccountId()));

        Account receiverAccount = null;
        Beneficiary beneficiary = null;

        if (request.getReceiverAccountId() != null) {
            receiverAccount = accountRepository.findById(request.getReceiverAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Receiver account", "id", request.getReceiverAccountId()));
        }

        if (request.getBeneficiaryId() != null) {
            beneficiary = beneficiaryRepository.findById(request.getBeneficiaryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Beneficiary", "id", request.getBeneficiaryId()));
        }

        LocalDate nextExecutionDate = calculateNextExecutionDate(
                request.getStartDate(),
                ScheduleFrequency.valueOf(request.getFrequency())
        );

        ScheduledPayment scheduledPayment = ScheduledPayment.builder()
                .customer(customer)
                .senderAccount(senderAccount)
                .receiverAccount(receiverAccount)
                .beneficiary(beneficiary)
                .receiverAccountNumber(request.getReceiverAccountNumber())
                .receiverIfscCode(request.getReceiverIfscCode())
                .receiverName(request.getReceiverName())
                .amount(request.getAmount())
                .transactionType(TransactionType.valueOf(request.getTransactionType()))
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .description(request.getDescription())
                .frequency(ScheduleFrequency.valueOf(request.getFrequency()))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .nextExecutionDate(nextExecutionDate)
                .executionTime(request.getExecutionTime() != null ? request.getExecutionTime() : LocalTime.of(9, 0))
                .active(true)
                .build();

        scheduledPayment = scheduledPaymentRepository.save(scheduledPayment);
        log.info("Scheduled payment created: {} for customer: {}", scheduledPayment.getId(), customer.getId());

        return mapToResponse(scheduledPayment);
    }

    public List<ScheduledPaymentResponse> getMyScheduledPayments(String email) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        List<ScheduledPayment> scheduledPayments = scheduledPaymentRepository
                .findByCustomerOrderByCreatedAtDesc(customer);

        return scheduledPayments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduledPaymentResponse updateScheduledPayment(Long id, SchedulePaymentRequest request, String email) {
        ScheduledPayment scheduledPayment = scheduledPaymentRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled payment", "id", id));

        if (request.getAmount() != null) {
            scheduledPayment.setAmount(request.getAmount());
        }

        if (request.getDescription() != null) {
            scheduledPayment.setDescription(request.getDescription());
        }

        if (request.getFrequency() != null) {
            scheduledPayment.setFrequency(ScheduleFrequency.valueOf(request.getFrequency()));
            scheduledPayment.setNextExecutionDate(
                    calculateNextExecutionDate(LocalDate.now(), scheduledPayment.getFrequency())
            );
        }

        if (request.getExecutionTime() != null) {
            scheduledPayment.setExecutionTime(request.getExecutionTime());
        }

        scheduledPayment = scheduledPaymentRepository.save(scheduledPayment);
        log.info("Scheduled payment updated: {}", id);

        return mapToResponse(scheduledPayment);
    }

    @Transactional
    public void pauseScheduledPayment(Long id, String email) {
        ScheduledPayment scheduledPayment = scheduledPaymentRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled payment", "id", id));

        scheduledPayment.setPaused(true);
        scheduledPaymentRepository.save(scheduledPayment);

        log.info("Scheduled payment paused: {}", id);
    }

    @Transactional
    public void resumeScheduledPayment(Long id, String email) {
        ScheduledPayment scheduledPayment = scheduledPaymentRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled payment", "id", id));

        scheduledPayment.setPaused(false);
        scheduledPaymentRepository.save(scheduledPayment);

        log.info("Scheduled payment resumed: {}", id);
    }

    @Transactional
    public void cancelScheduledPayment(Long id, String email) {
        ScheduledPayment scheduledPayment = scheduledPaymentRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled payment", "id", id));

        scheduledPayment.setActive(false);
        scheduledPaymentRepository.save(scheduledPayment);

        log.info("Scheduled payment cancelled: {}", id);
    }

    private LocalDate calculateNextExecutionDate(LocalDate startDate, ScheduleFrequency frequency) {
        LocalDate today = LocalDate.now();
        LocalDate nextDate = startDate;

        while (nextDate.isBefore(today)) {
            nextDate = getNextDate(nextDate, frequency);
        }

        return nextDate;
    }

    private LocalDate getNextDate(LocalDate currentDate, ScheduleFrequency frequency) {
        switch (frequency) {
            case DAILY:
                return currentDate.plusDays(1);
            case WEEKLY:
                return currentDate.plusWeeks(1);
            case MONTHLY:
                return currentDate.plusMonths(1);
            case YEARLY:
                return currentDate.plusYears(1);
            default:
                return currentDate;
        }
    }

    private ScheduledPaymentResponse mapToResponse(ScheduledPayment sp) {
        return ScheduledPaymentResponse.builder()
                .id(sp.getId())
                .senderAccountNumber(sp.getSenderAccount().getAccountNumber())
                .receiverAccountNumber(sp.getReceiverAccount() != null ?
                        sp.getReceiverAccount().getAccountNumber() : sp.getReceiverAccountNumber())
                .receiverName(sp.getReceiverName())
                .amount(sp.getAmount())
                .transactionType(sp.getTransactionType().name())
                .paymentMethod(sp.getPaymentMethod().name())
                .description(sp.getDescription())
                .frequency(sp.getFrequency())
                .startDate(sp.getStartDate())
                .endDate(sp.getEndDate())
                .nextExecutionDate(sp.getNextExecutionDate())
                .executionTime(sp.getExecutionTime())
                .active(sp.isActive())
                .paused(sp.isPaused())
                .totalExecutions(sp.getTotalExecutions())
                .successfulExecutions(sp.getSuccessfulExecutions())
                .failedExecutions(sp.getFailedExecutions())
                .lastExecutedAt(sp.getLastExecutedAt())
                .lastExecutionStatus(sp.getLastExecutionStatus())
                .createdAt(sp.getCreatedAt())
                .build();
    }
}