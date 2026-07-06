package com.dvein.banking_backend.transaction.job;

import com.dvein.banking_backend.transaction.enums.UpiStatus;
import com.dvein.banking_backend.transaction.model.UpiCollectRequest;
import com.dvein.banking_backend.transaction.repository.UpiCollectRequestRepository;
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
public class ExpireCollectRequestJob {

    private final UpiCollectRequestRepository collectRequestRepository;

    @Scheduled(cron = "0 0 * * * ?") // Every hour
    @Transactional
    public void expireOldCollectRequests() {
        log.debug("Starting UPI collect request expiry job");

        LocalDateTime now = LocalDateTime.now();
        List<UpiCollectRequest> expiredRequests = collectRequestRepository
                .findByStatusAndExpiresAtBefore(UpiStatus.PENDING_VERIFICATION, now);

        log.info("Found {} expired UPI collect requests", expiredRequests.size());

        for (UpiCollectRequest request : expiredRequests) {
            request.setStatus(UpiStatus.INACTIVE);
            collectRequestRepository.save(request);
        }

        log.info("Expired {} UPI collect requests", expiredRequests.size());
    }
}