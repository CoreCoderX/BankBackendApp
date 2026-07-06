package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.transaction.enums.TransactionStatus;
import com.dvein.banking_backend.transaction.model.Transaction;
import com.dvein.banking_backend.transaction.model.TransactionApproval;
import com.dvein.banking_backend.transaction.repository.TransactionApprovalRepository;
import com.dvein.banking_backend.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionApprovalService {

    private final TransactionApprovalRepository approvalRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionExecutionService executionService;
    private final TransactionNotificationService notificationService;

    @Transactional
    public TransactionApproval createApprovalRequest(Transaction transaction) {
        if (approvalRepository.existsByTransaction(transaction)) {
            throw new InvalidRequestException("Approval request already exists for this transaction");
        }

        TransactionApproval approval = TransactionApproval.builder()
                .transaction(transaction)
                .requiresApproval(true)
                .approved(false)
                .build();

        approval = approvalRepository.save(approval);

        log.info("Approval request created for transaction: {}", transaction.getTransactionId());

        return approval;
    }

    @Transactional
    public void approveTransaction(Long approvalId, Long adminUserId, String adminEmail) {
        TransactionApproval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval", "id", approvalId));

        if (approval.isApproved()) {
            throw new InvalidRequestException("Transaction already approved");
        }

        approval.setApproved(true);
        approval.setApprovedBy(com.dvein.banking_backend.auth.model.User.builder().id(adminUserId).build());
        approval.setApprovedAt(LocalDateTime.now());

        approvalRepository.save(approval);

        // Execute the pending transaction
        Transaction transaction = approval.getTransaction();
        executionService.executeTransaction(transaction);

        // Send notification
        notificationService.sendTransactionNotification(transaction);

        log.info("Transaction approved: {} by admin: {}", transaction.getTransactionId(), adminEmail);
    }

    @Transactional
    public void rejectTransaction(Long approvalId, Long adminUserId, String reason, String adminEmail) {
        TransactionApproval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval", "id", approvalId));

        if (approval.isApproved()) {
            throw new InvalidRequestException("Transaction already approved");
        }

        approval.setApproved(false);
        approval.setRejectionReason(reason);
        approval.setApprovedBy(com.dvein.banking_backend.auth.model.User.builder().id(adminUserId).build());

        approvalRepository.save(approval);

        // Update transaction status
        Transaction transaction = approval.getTransaction();
        transaction.setStatus(TransactionStatus.CANCELLED);
        transaction.setFailureReason("Transaction rejected by admin: " + reason);
        transaction.setFailedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        log.info("Transaction rejected: {} by admin: {} - Reason: {}", transaction.getTransactionId(), adminEmail, reason);
    }

    public Page<TransactionApproval> getPendingApprovals(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return approvalRepository.findByRequiresApprovalTrueAndApprovedFalse(pageable);
    }

    public List<TransactionApproval> getAllPendingApprovals() {
        return approvalRepository.findByRequiresApprovalTrueAndApprovedFalse();
    }

    public boolean requiresApproval(Transaction transaction) {
        return approvalRepository.existsByTransaction(transaction);
    }
}