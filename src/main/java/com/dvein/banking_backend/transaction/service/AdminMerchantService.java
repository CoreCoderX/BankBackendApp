package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.common.exception.DuplicateResourceException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.transaction.dto.request.CreateMerchantRequest;
import com.dvein.banking_backend.transaction.dto.request.UpdateMerchantRequest;
import com.dvein.banking_backend.transaction.dto.response.MerchantResponse;
import com.dvein.banking_backend.transaction.model.Merchant;
import com.dvein.banking_backend.transaction.model.MerchantCategory;
import com.dvein.banking_backend.transaction.repository.MerchantCategoryRepository;
import com.dvein.banking_backend.transaction.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantCategoryRepository categoryRepository;

    @Transactional
    public MerchantResponse createMerchant(CreateMerchantRequest request) {
        if (merchantRepository.existsByMerchantCode(request.getMerchantCode())) {
            throw new DuplicateResourceException("Merchant", "merchantCode");
        }

        MerchantCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Merchant category", "id", request.getCategoryId()));

        Merchant merchant = Merchant.builder()
                .merchantCode(request.getMerchantCode())
                .merchantName(request.getMerchantName())
                .category(category)
                .upiId(request.getUpiId())
                .verified(false)
                .active(true)
                .build();

        merchant = merchantRepository.save(merchant);
        log.info("Merchant created: {}", merchant.getMerchantCode());

        return mapToResponse(merchant);
    }

    @Transactional
    public MerchantResponse updateMerchant(Long merchantId, UpdateMerchantRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", "id", merchantId));

        if (request.getMerchantName() != null) {
            merchant.setMerchantName(request.getMerchantName());
        }

        if (request.getCategoryId() != null) {
            MerchantCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            merchant.setCategory(category);
        }

        if (request.getUpiId() != null) {
            merchant.setUpiId(request.getUpiId());
        }

        if (request.getVerified() != null) {
            merchant.setVerified(request.getVerified());
        }

        if (request.getActive() != null) {
            merchant.setActive(request.getActive());
        }

        merchant = merchantRepository.save(merchant);
        log.info("Merchant updated: {}", merchantId);

        return mapToResponse(merchant);
    }

    public List<MerchantResponse> getAllMerchants(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("merchantName"));
        Page<Merchant> merchantPage = merchantRepository.findAll(pageable);

        return merchantPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMerchant(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", "id", merchantId));

        merchant.setActive(false);
        merchantRepository.save(merchant);

        log.info("Merchant deactivated: {}", merchantId);
    }

    private MerchantResponse mapToResponse(Merchant merchant) {
        return MerchantResponse.builder()
                .id(merchant.getId())
                .merchantCode(merchant.getMerchantCode())
                .merchantName(merchant.getMerchantName())
                .categoryName(merchant.getCategory() != null ? merchant.getCategory().getName() : null)
                .upiId(merchant.getUpiId())
                .verified(merchant.isVerified())
                .active(merchant.isActive())
                .createdAt(merchant.getCreatedAt())
                .build();
    }
}