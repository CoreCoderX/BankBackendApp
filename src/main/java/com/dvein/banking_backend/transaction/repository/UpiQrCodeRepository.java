package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.UpiQrCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UpiQrCodeRepository extends JpaRepository<UpiQrCode, Long> {

    Optional<UpiQrCode> findByQrId(String qrId);

    List<UpiQrCode> findByUpiIdOrderByCreatedAtDesc(String upiId);

    List<UpiQrCode> findByUpiIdAndActiveTrueOrderByCreatedAtDesc(String upiId);

    List<UpiQrCode> findByExpiresAtBeforeAndActiveTrue(LocalDateTime cutoffTime);
}