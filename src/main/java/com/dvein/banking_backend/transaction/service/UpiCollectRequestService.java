package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.transaction.dto.request.UpiCollectMoneyRequest;
import com.dvein.banking_backend.transaction.dto.response.UpiCollectRequestResponse;
import com.dvein.banking_backend.transaction.enums.UpiStatus;
import com.dvein.banking_backend.transaction.exception.InvalidUpiIdException;
import com.dvein.banking_backend.transaction.model.UpiCollectRequest;
import com.dvein.banking_backend.transaction.model.UpiId;
import com.dvein.banking_backend.transaction.repository.UpiCollectRequestRepository;
import com.dvein.banking_backend.transaction.repository.UpiIdRepository;
import com.dvein.banking_backend.transaction.util.TransactionIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpiCollectRequestService {

    private final UpiCollectRequestRepository collectRequestRepository;
    private final UpiIdRepository upiIdRepository;
    private final TransactionIdGenerator idGenerator;

    private static final int REQUEST_EXPIRY_HOURS = 24;

    @Transactional
    public UpiCollectRequestResponse createCollectRequest(UpiCollectMoneyRequest request, String email) {
        // Validate requester UPI ID (must belong to current user)
        UpiId requesterUpiId = upiIdRepository.findByUpiId(request.getRequesterUpiId())
                .orElseThrow(() -> new InvalidUpiIdException("Requester UPI ID not found"));

        if (!requesterUpiId.getUpiProfile().getCustomer().getUser().getEmail().equals(email)) {
            throw new InvalidRequestException("You do not own the requester UPI ID");
        }

        // Validate payer UPI ID exists
        if (!upiIdRepository.existsByUpiId(request.getPayerUpiId())) {
            throw new InvalidUpiIdException("Payer UPI ID not found: " + request.getPayerUpiId());
        }

        if (request.getRequesterUpiId().equals(request.getPayerUpiId())) {
            throw new InvalidRequestException("Cannot request money from yourself");
        }

        String requestId = idGenerator.generateRequestId();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(REQUEST_EXPIRY_HOURS);

        UpiCollectRequest collectRequest = UpiCollectRequest.builder()
                .requestId(requestId)
                .requesterUpiId(request.getRequesterUpiId())
                .payerUpiId(request.getPayerUpiId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(UpiStatus.PENDING_VERIFICATION)
                .expiresAt(expiresAt)
                .build();

        collectRequest = collectRequestRepository.save(collectRequest);
        log.info("UPI collect request created: {} from {} to {}",
                requestId, request.getRequesterUpiId(), request.getPayerUpiId());

        return mapToResponse(collectRequest);
    }

    public List<UpiCollectRequestResponse> getPendingRequestsForPayer(String payerUpiId, String email) {
        UpiId upiId = upiIdRepository.findByUpiId(payerUpiId)
                .orElseThrow(() -> new InvalidUpiIdException("UPI ID not found"));

        if (!upiId.getUpiProfile().getCustomer().getUser().getEmail().equals(email)) {
            throw new InvalidRequestException("You do not own this UPI ID");
        }

        List<UpiCollectRequest> requests = collectRequestRepository
                .findByPayerUpiIdAndStatus(payerUpiId, UpiStatus.PENDING_VERIFICATION);

        return requests.stream()
                .filter(req -> !req.isExpired())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UpiCollectRequestResponse> getMyCollectRequests(String requesterUpiId, String email) {
        UpiId upiId = upiIdRepository.findByUpiId(requesterUpiId)
                .orElseThrow(() -> new InvalidUpiIdException("UPI ID not found"));

        if (!upiId.getUpiProfile().getCustomer().getUser().getEmail().equals(email)) {
            throw new InvalidRequestException("You do not own this UPI ID");
        }

        List<UpiCollectRequest> requests = collectRequestRepository
                .findByRequesterUpiIdOrderByCreatedAtDesc(requesterUpiId);

        return requests.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveCollectRequest(String requestId, String email) {
        UpiCollectRequest request = collectRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Collect request", "requestId", requestId));

        if (request.isExpired()) {
            throw new InvalidRequestException("Collect request has expired");
        }

        UpiId payerUpiId = upiIdRepository.findByUpiId(request.getPayerUpiId())
                .orElseThrow(() -> new InvalidUpiIdException("Payer UPI ID not found"));

        if (!payerUpiId.getUpiProfile().getCustomer().getUser().getEmail().equals(email)) {
            throw new InvalidRequestException("You are not authorized to approve this request");
        }

        request.setStatus(UpiStatus.ACTIVE);
        request.setRespondedAt(LocalDateTime.now());
        collectRequestRepository.save(request);

        log.info("UPI collect request approved: {}", requestId);
    }

    @Transactional
    public void rejectCollectRequest(String requestId, String email) {
        UpiCollectRequest request = collectRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Collect request", "requestId", requestId));

        UpiId payerUpiId = upiIdRepository.findByUpiId(request.getPayerUpiId())
                .orElseThrow(() -> new InvalidUpiIdException("Payer UPI ID not found"));

        if (!payerUpiId.getUpiProfile().getCustomer().getUser().getEmail().equals(email)) {
            throw new InvalidRequestException("You are not authorized to reject this request");
        }

        request.setStatus(UpiStatus.BLOCKED);
        request.setRespondedAt(LocalDateTime.now());
        collectRequestRepository.save(request);

        log.info("UPI collect request rejected: {}", requestId);
    }

    private UpiCollectRequestResponse mapToResponse(UpiCollectRequest request) {
        return UpiCollectRequestResponse.builder()
                .requestId(request.getRequestId())
                .requesterUpiId(request.getRequesterUpiId())
                .payerUpiId(request.getPayerUpiId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(request.getStatus())
                .expiresAt(request.getExpiresAt())
                .respondedAt(request.getRespondedAt())
                .createdAt(request.getCreatedAt())
                .expired(request.isExpired())
                .build();
    }
}