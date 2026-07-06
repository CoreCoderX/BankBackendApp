package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Beneficiary;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.account.repository.BeneficiaryRepository;
import com.dvein.banking_backend.account.repository.CustomerRepository;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.transaction.dto.request.CreateStandingInstructionRequest;
import com.dvein.banking_backend.transaction.dto.response.StandingInstructionResponse;
import com.dvein.banking_backend.transaction.enums.PaymentMethod;
import com.dvein.banking_backend.transaction.enums.ScheduleFrequency;
import com.dvein.banking_backend.transaction.enums.TransactionType;
import com.dvein.banking_backend.transaction.model.StandingInstruction;
import com.dvein.banking_backend.transaction.repository.StandingInstructionRepository;
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
public class StandingInstructionService {

    private final StandingInstructionRepository siRepository;
    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public StandingInstructionResponse createSI(CreateStandingInstructionRequest request, String email) {
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

        StandingInstruction si = StandingInstruction.builder()
                .customer(customer)
                .senderAccount(senderAccount)
                .receiverAccount(receiverAccount)
                .beneficiary(beneficiary)
                .receiverAccountNumber(request.getReceiverAccountNumber())
                .receiverIfscCode(request.getReceiverIfscCode())
                .receiverName(request.getReceiverName())
                .maxAmount(request.getMaxAmount())
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

        si = siRepository.save(si);
        log.info("Standing instruction created: {} for customer: {}", si.getId(), customer.getId());

        return mapToResponse(si);
    }

    public List<StandingInstructionResponse> getMySIs(String email) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        List<StandingInstruction> sis = siRepository.findByCustomerOrderByCreatedAtDesc(customer);

        return sis.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void pauseSI(Long id, String email) {
        StandingInstruction si = siRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Standing instruction", "id", id));

        si.setPaused(true);
        siRepository.save(si);

        log.info("Standing instruction paused: {}", id);
    }

    @Transactional
    public void resumeSI(Long id, String email) {
        StandingInstruction si = siRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Standing instruction", "id", id));

        si.setPaused(false);
        siRepository.save(si);

        log.info("Standing instruction resumed: {}", id);
    }

    @Transactional
    public void deleteSI(Long id, String email) {
        StandingInstruction si = siRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Standing instruction", "id", id));

        si.setActive(false);
        siRepository.save(si);

        log.info("Standing instruction deleted: {}", id);
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

    private StandingInstructionResponse mapToResponse(StandingInstruction si) {
        return StandingInstructionResponse.builder()
                .id(si.getId())
                .senderAccountNumber(si.getSenderAccount().getAccountNumber())
                .receiverAccountNumber(si.getReceiverAccount() != null ?
                        si.getReceiverAccount().getAccountNumber() : si.getReceiverAccountNumber())
                .receiverName(si.getReceiverName())
                .maxAmount(si.getMaxAmount())
                .transactionType(si.getTransactionType().name())
                .paymentMethod(si.getPaymentMethod().name())
                .description(si.getDescription())
                .frequency(si.getFrequency())
                .startDate(si.getStartDate())
                .endDate(si.getEndDate())
                .nextExecutionDate(si.getNextExecutionDate())
                .executionTime(si.getExecutionTime())
                .active(si.isActive())
                .paused(si.isPaused())
                .totalExecutions(si.getTotalExecutions())
                .successfulExecutions(si.getSuccessfulExecutions())
                .failedExecutions(si.getFailedExecutions())
                .lastExecutedAt(si.getLastExecutedAt())
                .createdAt(si.getCreatedAt())
                .build();
    }
}