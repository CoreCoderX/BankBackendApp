package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.enums.UpiStatus;
import com.dvein.banking_backend.transaction.model.UpiCollectRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UpiCollectRequestRepository extends JpaRepository<UpiCollectRequest, Long> {

    Optional<UpiCollectRequest> findByRequestId(String requestId);

    List<UpiCollectRequest> findByPayerUpiIdAndStatus(String payerUpiId, UpiStatus status);

    List<UpiCollectRequest> findByRequesterUpiIdAndStatus(String requesterUpiId, UpiStatus status);

    List<UpiCollectRequest> findByStatusAndExpiresAtBefore(UpiStatus status, LocalDateTime cutoffTime);

    List<UpiCollectRequest> findByPayerUpiIdOrderByCreatedAtDesc(String payerUpiId);

    List<UpiCollectRequest> findByRequesterUpiIdOrderByCreatedAtDesc(String requesterUpiId);
}