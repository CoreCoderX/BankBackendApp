package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.transaction.dto.request.ScheduledPaymentRequest;
import com.dvein.banking_backend.transaction.dto.response.ScheduledPaymentResponse;
import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.mapper.ScheduledPaymentMapper;
import com.dvein.banking_backend.transaction.model.Beneficiary;
import com.dvein.banking_backend.transaction.model.ScheduledPayment;
import com.dvein.banking_backend.transaction.repository.BeneficiaryRepository;
import com.dvein.banking_backend.transaction.repository.ScheduledPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledPaymentService {

    private final ScheduledPaymentRepository scheduledPaymentRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final ScheduledPaymentMapper scheduledPaymentMapper;

    @Transactional
    public ApiResponse<ScheduledPaymentResponse> createScheduledPayment(ScheduledPaymentRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Beneficiary beneficiary = beneficiaryRepository.findByIdAndUserId(request.getBeneficiaryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));

        if (!beneficiary.getIsActive()) {
            throw new InvalidRequestException("Beneficiary is not active");
        }

        ScheduledPayment scheduledPayment = ScheduledPayment.builder()
                .user(user)
                .beneficiary(beneficiary)
                .amount(request.getAmount())
                .frequency(request.getFrequency())
                .nextExecutionDate(request.getStartDate())
                .status(TransactionStatus.PENDING)
                .remarks(request.getRemarks())
                .build();

        ScheduledPayment saved = scheduledPaymentRepository.save(scheduledPayment);

        log.info("Scheduled payment created: {}", saved.getId());

        return ApiResponse.success(
                "Scheduled payment created successfully",
                scheduledPaymentMapper.toResponse(saved)
        );
    }

    public ApiResponse<List<ScheduledPaymentResponse>> getAllScheduledPayments() {
        Long userId = SecurityContextHelper.getCurrentUserId();

        List<ScheduledPayment> scheduledPayments = scheduledPaymentRepository.findByUserId(userId);

        List<ScheduledPaymentResponse> responses = scheduledPayments.stream()
                .map(scheduledPaymentMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Scheduled payments retrieved successfully", responses);
    }

    public ApiResponse<ScheduledPaymentResponse> getScheduledPaymentById(Long id) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        ScheduledPayment scheduledPayment = scheduledPaymentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled payment not found"));

        return ApiResponse.success(
                "Scheduled payment retrieved successfully",
                scheduledPaymentMapper.toResponse(scheduledPayment)
        );
    }

    @Transactional
    public ApiResponse<ScheduledPaymentResponse> updateScheduledPayment(Long id, ScheduledPaymentRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        ScheduledPayment scheduledPayment = scheduledPaymentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled payment not found"));

        Beneficiary beneficiary = beneficiaryRepository.findByIdAndUserId(request.getBeneficiaryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));

        scheduledPayment.setBeneficiary(beneficiary);
        scheduledPayment.setAmount(request.getAmount());
        scheduledPayment.setFrequency(request.getFrequency());
        scheduledPayment.setNextExecutionDate(request.getStartDate());
        scheduledPayment.setRemarks(request.getRemarks());

        ScheduledPayment updated = scheduledPaymentRepository.save(scheduledPayment);

        log.info("Scheduled payment updated: {}", id);

        return ApiResponse.success(
                "Scheduled payment updated successfully",
                scheduledPaymentMapper.toResponse(updated)
        );
    }

    @Transactional
    public ApiResponse<Void> deleteScheduledPayment(Long id) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        ScheduledPayment scheduledPayment = scheduledPaymentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled payment not found"));

        scheduledPaymentRepository.delete(scheduledPayment);

        log.info("Scheduled payment deleted: {}", id);

        return ApiResponse.success("Scheduled payment deleted successfully", null);
    }

    @Transactional
    public ApiResponse<Void> pauseScheduledPayment(Long id) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        ScheduledPayment scheduledPayment = scheduledPaymentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled payment not found"));

        scheduledPayment.setStatus(TransactionStatus.FAILED); // Using FAILED as paused

        scheduledPaymentRepository.save(scheduledPayment);

        log.info("Scheduled payment paused: {}", id);

        return ApiResponse.success("Scheduled payment paused successfully", null);
    }

    @Transactional
    public ApiResponse<Void> resumeScheduledPayment(Long id) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        ScheduledPayment scheduledPayment = scheduledPaymentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled payment not found"));

        scheduledPayment.setStatus(TransactionStatus.PENDING);

        scheduledPaymentRepository.save(scheduledPayment);

        log.info("Scheduled payment resumed: {}", id);

        return ApiResponse.success("Scheduled payment resumed successfully", null);
    }
}