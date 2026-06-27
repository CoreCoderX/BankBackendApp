package com.dvein.banking_backend.FundTransaction.service;

import com.dvein.banking_backend.common.dto.ApiResponse;
import com.dvein.banking_backend.common.dto.PageResponse;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import com.dvein.banking_backend.FundTransaction.dto.response.MiniStatementResponse;
import com.dvein.banking_backend.FundTransaction.dto.response.TransactionDetailResponse;
import com.dvein.banking_backend.FundTransaction.dto.response.TransactionHistoryResponse;
import com.dvein.banking_backend.FundTransaction.dto.response.TransactionResponse;
import com.dvein.banking_backend.FundTransaction.enums.TransactionStatus;
import com.dvein.banking_backend.FundTransaction.enums.TransactionType;
import com.dvein.banking_backend.FundTransaction.mapper.TransactionMapper;
import com.dvein.banking_backend.FundTransaction.model.Transaction;
import com.dvein.banking_backend.FundTransaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public ApiResponse<PageResponse<TransactionResponse>> getAllTransactions(
            int page, int size, String sortBy, String sortDirection
    ) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Transaction> transactionPage = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<TransactionResponse> responses = transactionPage.getContent().stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());

        PageResponse<TransactionResponse> pageResponse = PageResponse.<TransactionResponse>builder()
                .content(responses)
                .pageNumber(transactionPage.getNumber())
                .pageSize(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .last(transactionPage.isLast())
                .build();

        return ApiResponse.success("Transactions retrieved successfully", pageResponse);
    }

    public ApiResponse<TransactionDetailResponse> getTransactionById(String transactionId) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Verify ownership
        if (!transaction.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        TransactionDetailResponse response = transactionMapper.toDetailResponse(transaction);

        return ApiResponse.success("Transaction details retrieved successfully", response);
    }

    public ApiResponse<PageResponse<TransactionHistoryResponse>> getTransactionHistory(
            LocalDate startDate,
            LocalDate endDate,
            TransactionStatus status,
            TransactionType type,
            int page,
            int size
    ) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        // Default to last 30 days if no dates provided
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Transaction> transactionPage;

        if (status != null && type != null) {
            transactionPage = transactionRepository.findByUserIdAndDateRangeAndStatusAndType(
                    userId, startDateTime, endDateTime, status, type, pageable
            );
        } else if (status != null) {
            transactionPage = transactionRepository.findByUserIdAndDateRangeAndStatus(
                    userId, startDateTime, endDateTime, status, pageable
            );
        } else if (type != null) {
            transactionPage = transactionRepository.findByUserIdAndDateRangeAndType(
                    userId, startDateTime, endDateTime, type, pageable
            );
        } else {
            transactionPage = transactionRepository.findByUserIdAndDateRange(
                    userId, startDateTime, endDateTime, pageable
            );
        }

        List<TransactionHistoryResponse> responses = transactionPage.getContent().stream()
                .map(transactionMapper::toHistoryResponse)
                .collect(Collectors.toList());

        PageResponse<TransactionHistoryResponse> pageResponse = PageResponse.<TransactionHistoryResponse>builder()
                .content(responses)
                .pageNumber(transactionPage.getNumber())
                .pageSize(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .last(transactionPage.isLast())
                .build();

        return ApiResponse.success("Transaction history retrieved successfully", pageResponse);
    }

    public ApiResponse<MiniStatementResponse> getMiniStatement() {
        Long userId = SecurityContextHelper.getCurrentUserId();

        List<Transaction> recentTransactions = transactionRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);

        List<TransactionResponse> transactions = recentTransactions.stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());

        MiniStatementResponse response = MiniStatementResponse.builder()
                .transactions(transactions)
                .totalTransactions(transactions.size())
                .generatedAt(LocalDateTime.now())
                .build();

        return ApiResponse.success("Mini statement generated successfully", response);
    }

    public ApiResponse<PageResponse<TransactionResponse>> searchTransactions(
            String transactionId,
            String accountNumber,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDate date,
            int page,
            int size
    ) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Transaction> transactionPage = transactionRepository.searchTransactions(
                userId, transactionId, accountNumber, minAmount, maxAmount, date, pageable
        );

        List<TransactionResponse> responses = transactionPage.getContent().stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());

        PageResponse<TransactionResponse> pageResponse = PageResponse.<TransactionResponse>builder()
                .content(responses)
                .pageNumber(transactionPage.getNumber())
                .pageSize(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .last(transactionPage.isLast())
                .build();

        return ApiResponse.success("Search results retrieved successfully", pageResponse);
    }

    public ApiResponse<PageResponse<TransactionResponse>> getTodayTransactions(int page, int size) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Transaction> transactionPage = transactionRepository.findByUserIdAndDateRange(
                userId, startOfDay, endOfDay, pageable
        );

        List<TransactionResponse> responses = transactionPage.getContent().stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());

        PageResponse<TransactionResponse> pageResponse = PageResponse.<TransactionResponse>builder()
                .content(responses)
                .pageNumber(transactionPage.getNumber())
                .pageSize(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .last(transactionPage.isLast())
                .build();

        return ApiResponse.success("Today's transactions retrieved successfully", pageResponse);
    }

    public ApiResponse<PageResponse<TransactionResponse>> getLastWeekTransactions(int page, int size) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        LocalDateTime now = LocalDateTime.now();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Transaction> transactionPage = transactionRepository.findByUserIdAndDateRange(
                userId, weekAgo, now, pageable
        );

        List<TransactionResponse> responses = transactionPage.getContent().stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());

        PageResponse<TransactionResponse> pageResponse = PageResponse.<TransactionResponse>builder()
                .content(responses)
                .pageNumber(transactionPage.getNumber())
                .pageSize(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .last(transactionPage.isLast())
                .build();

        return ApiResponse.success("Last week's transactions retrieved successfully", pageResponse);
    }

    public ApiResponse<PageResponse<TransactionResponse>> getLastMonthTransactions(int page, int size) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        LocalDateTime now = LocalDateTime.now();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Transaction> transactionPage = transactionRepository.findByUserIdAndDateRange(
                userId, monthAgo, now, pageable
        );

        List<TransactionResponse> responses = transactionPage.getContent().stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());

        PageResponse<TransactionResponse> pageResponse = PageResponse.<TransactionResponse>builder()
                .content(responses)
                .pageNumber(transactionPage.getNumber())
                .pageSize(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .last(transactionPage.isLast())
                .build();

        return ApiResponse.success("Last month's transactions retrieved successfully", pageResponse);
    }

    public ApiResponse<PageResponse<TransactionResponse>> getTransactionsByAccount(
            Long accountId, int page, int size
    ) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Transaction> transactionPage = transactionRepository.findByUserIdAndAccountId(
                userId, accountId, pageable
        );

        List<TransactionResponse> responses = transactionPage.getContent().stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());

        PageResponse<TransactionResponse> pageResponse = PageResponse.<TransactionResponse>builder()
                .content(responses)
                .pageNumber(transactionPage.getNumber())
                .pageSize(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .last(transactionPage.isLast())
                .build();

        return ApiResponse.success("Account transactions retrieved successfully", pageResponse);
    }
}