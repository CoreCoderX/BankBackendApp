package com.dvein.banking_backend.transaction.job;

import com.dvein.banking_backend.transaction.model.UpiQrCode;
import com.dvein.banking_backend.transaction.repository.UpiQrCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpireQrCodesJob {

    private final UpiQrCodeRepository qrCodeRepository;

    @Scheduled(cron = "0 0 * * * ?") // Every hour
    @Transactional
    public void expireOldQrCodes() {
        log.debug("Starting QR code expiry job");

        LocalDateTime now = LocalDateTime.now();
        List<UpiQrCode> expiredQrCodes = qrCodeRepository
                .findByExpiresAtBeforeAndActiveTrue(now);

        log.info("Found {} expired QR codes", expiredQrCodes.size());

        for (UpiQrCode qrCode : expiredQrCodes) {
            qrCode.setActive(false);
            qrCodeRepository.save(qrCode);
        }

        log.info("Expired {} QR codes", expiredQrCodes.size());
    }
}