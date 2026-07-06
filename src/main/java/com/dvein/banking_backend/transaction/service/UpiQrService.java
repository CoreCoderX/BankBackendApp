package com.dvein.banking_backend.transaction.service;

import com.dvein.banking_backend.common.exception.InvalidRequestException;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import com.dvein.banking_backend.transaction.dto.request.GenerateQrRequest;
import com.dvein.banking_backend.transaction.dto.response.UpiQrResponse;
import com.dvein.banking_backend.transaction.enums.QrType;
import com.dvein.banking_backend.transaction.exception.InvalidUpiIdException;
import com.dvein.banking_backend.transaction.model.UpiId;
import com.dvein.banking_backend.transaction.model.UpiQrCode;
import com.dvein.banking_backend.transaction.repository.UpiIdRepository;
import com.dvein.banking_backend.transaction.repository.UpiQrCodeRepository;
import com.dvein.banking_backend.transaction.util.TransactionIdGenerator;
import com.dvein.banking_backend.transaction.util.UpiQrCodeGenerator;
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
public class UpiQrService {

    private final UpiQrCodeRepository qrCodeRepository;
    private final UpiIdRepository upiIdRepository;
    private final UpiQrCodeGenerator qrCodeGenerator;
    private final TransactionIdGenerator idGenerator;

    @Transactional
    public UpiQrResponse generateQrCode(GenerateQrRequest request, String email) {
        UpiId upiId = upiIdRepository.findByUpiId(request.getUpiId())
                .orElseThrow(() -> new InvalidUpiIdException("UPI ID not found: " + request.getUpiId()));

        // Verify ownership
        if (!upiId.getUpiProfile().getCustomer().getUser().getEmail().equals(email)) {
            throw new InvalidRequestException("You do not own this UPI ID");
        }

        QrType qrType = QrType.valueOf(request.getQrType());

        if (qrType == QrType.DYNAMIC && request.getAmount() == null) {
            throw new InvalidRequestException("Amount is required for DYNAMIC QR code");
        }

        String qrId = idGenerator.generateQrId();
        String qrData = qrCodeGenerator.generateUpiQrData(
                request.getUpiId(),
                upiId.getUpiProfile().getCustomer().getFullName(),
                request.getAmount(),
                request.getDescription()
        );

        String qrImageBase64 = qrCodeGenerator.generateQrImage(qrData);

        LocalDateTime expiresAt = null;
        if (qrType == QrType.DYNAMIC && request.getExpiryHours() != null) {
            expiresAt = LocalDateTime.now().plusHours(request.getExpiryHours());
        }

        UpiQrCode qrCode = UpiQrCode.builder()
                .qrId(qrId)
                .upiId(request.getUpiId())
                .qrType(qrType)
                .amount(request.getAmount())
                .description(request.getDescription())
                .qrData(qrData)
                .qrImageBase64(qrImageBase64)
                .active(true)
                .expiresAt(expiresAt)
                .maxScans(request.getMaxScans())
                .build();

        qrCode = qrCodeRepository.save(qrCode);
        log.info("QR code generated: {} for UPI ID: {}", qrId, request.getUpiId());

        return mapToQrResponse(qrCode);
    }

    public UpiQrResponse getQrCode(String qrId, String email) {
        UpiQrCode qrCode = qrCodeRepository.findByQrId(qrId)
                .orElseThrow(() -> new ResourceNotFoundException("QR code", "qrId", qrId));

        // Verify ownership
        UpiId upiId = upiIdRepository.findByUpiId(qrCode.getUpiId())
                .orElseThrow(() -> new InvalidUpiIdException("UPI ID not found"));

        if (!upiId.getUpiProfile().getCustomer().getUser().getEmail().equals(email)) {
            throw new InvalidRequestException("You do not own this QR code");
        }

        return mapToQrResponse(qrCode);
    }

    public List<UpiQrResponse> getMyQrCodes(String upiId, String email) {
        UpiId upiIdEntity = upiIdRepository.findByUpiId(upiId)
                .orElseThrow(() -> new InvalidUpiIdException("UPI ID not found"));

        if (!upiIdEntity.getUpiProfile().getCustomer().getUser().getEmail().equals(email)) {
            throw new InvalidRequestException("You do not own this UPI ID");
        }

        List<UpiQrCode> qrCodes = qrCodeRepository.findByUpiIdOrderByCreatedAtDesc(upiId);

        return qrCodes.stream()
                .map(this::mapToQrResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deactivateQrCode(String qrId, String email) {
        UpiQrCode qrCode = qrCodeRepository.findByQrId(qrId)
                .orElseThrow(() -> new ResourceNotFoundException("QR code", "qrId", qrId));

        UpiId upiId = upiIdRepository.findByUpiId(qrCode.getUpiId())
                .orElseThrow(() -> new InvalidUpiIdException("UPI ID not found"));

        if (!upiId.getUpiProfile().getCustomer().getUser().getEmail().equals(email)) {
            throw new InvalidRequestException("You do not own this QR code");
        }

        qrCode.setActive(false);
        qrCodeRepository.save(qrCode);

        log.info("QR code deactivated: {}", qrId);
    }

    @Transactional
    public void incrementScanCount(UpiQrCode qrCode) {
        qrCode.incrementScanCount();
        qrCodeRepository.save(qrCode);
    }

    public UpiQrCode validateQrCode(String qrData) {
        UpiQrCodeGenerator.UpiQrDataParsed parsed = qrCodeGenerator.parseUpiQrData(qrData);

        if (parsed.getPayeeAddress() == null) {
            throw new InvalidRequestException("Invalid QR code data");
        }

        // For DYNAMIC QR, find the QR code record
        // For STATIC QR, we just need to verify the UPI ID exists
        UpiId upiId = upiIdRepository.findByUpiId(parsed.getPayeeAddress())
                .orElseThrow(() -> new InvalidUpiIdException("Invalid UPI ID in QR code"));

        // Return a virtual QR code for STATIC QR
        return UpiQrCode.builder()
                .upiId(parsed.getPayeeAddress())
                .qrType(QrType.STATIC)
                .amount(parsed.getAmount())
                .description(parsed.getTransactionNote())
                .active(true)
                .build();
    }

    private UpiQrResponse mapToQrResponse(UpiQrCode qrCode) {
        return UpiQrResponse.builder()
                .qrId(qrCode.getQrId())
                .upiId(qrCode.getUpiId())
                .qrType(qrCode.getQrType())
                .amount(qrCode.getAmount())
                .description(qrCode.getDescription())
                .qrData(qrCode.getQrData())
                .qrImageBase64(qrCode.getQrImageBase64())
                .active(qrCode.isActive())
                .expiresAt(qrCode.getExpiresAt())
                .scanCount(qrCode.getScanCount())
                .maxScans(qrCode.getMaxScans())
                .createdAt(qrCode.getCreatedAt())
                .build();
    }
}