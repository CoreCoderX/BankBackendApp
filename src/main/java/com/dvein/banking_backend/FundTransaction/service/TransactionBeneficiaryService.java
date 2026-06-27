package com.dvein.banking_backend.FundTransaction.service;

import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.FundTransaction.dto.request.AddBeneficiaryRequest;
import com.dvein.banking_backend.FundTransaction.dto.response.BeneficiaryResponse;
import com.dvein.banking_backend.FundTransaction.mapper.BeneficiaryMapper;
import com.dvein.banking_backend.FundTransaction.model.Beneficiary;
import com.dvein.banking_backend.FundTransaction.repository.BeneficiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionBeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final BeneficiaryMapper beneficiaryMapper;

    @Transactional
    public ApiResponse<BeneficiaryResponse> addBeneficiary(AddBeneficiaryRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        // Check if beneficiary already exists
        if (beneficiaryRepository.existsByUserIdAndAccountNumber(userId, request.getAccountNumber())) {
            throw new InvalidRequestException("Beneficiary with this account number already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Beneficiary beneficiary = Beneficiary.builder()
                .user(user)
                .nickname(request.getNickname())
                .accountNumber(request.getAccountNumber())
                .ifscCode(request.getIfscCode())
                .bank_name(request.getBank_name())
                .branchName(request.getBranchName())
                .isActive(true)
                .isVerified(false)
                .build();

        Beneficiary savedBeneficiary = beneficiaryRepository.save(beneficiary);

        log.info("Beneficiary added successfully for user: {}", userId);

        return ApiResponse.success(
                "Beneficiary added successfully",
                beneficiaryMapper.toResponse(savedBeneficiary)
        );
    }

    public ApiResponse<List<BeneficiaryResponse>> getAllBeneficiaries() {
        Long userId = SecurityContextHelper.getCurrentUserId();

        List<Beneficiary> beneficiaries = beneficiaryRepository.findByUserIdAndIsActiveTrue(userId);

        List<BeneficiaryResponse> responses = beneficiaries.stream()
                .map(beneficiaryMapper::toResponse)
                .toList();

        return ApiResponse.success("Beneficiaries retrieved successfully", responses);
    }

    @Transactional
    public ApiResponse<Void> deleteBeneficiary(Long beneficiaryId) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        Beneficiary beneficiary = beneficiaryRepository.findByIdAndUserId(beneficiaryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));

        beneficiary.setIsActive(false);
        beneficiaryRepository.save(beneficiary);

        log.info("Beneficiary deleted for user: {}", userId);

        return ApiResponse.success("Beneficiary deleted successfully", null);
    }
}