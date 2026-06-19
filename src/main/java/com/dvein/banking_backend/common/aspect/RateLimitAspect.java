package com.dvein.banking_backend.common.aspect;

import com.dvein.banking_backend.common.annotation.RateLimited;
import com.dvein.banking_backend.common.exception.CustomException;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Aspect for implementing rate limiting on endpoints
 * Uses in-memory storage for tracking request counts
 * For production, consider using Redis for distributed rate limiting
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final SecurityContextHelper securityContextHelper;

    @Value("${rate.limit.enabled:true}")
    private boolean rateLimitEnabled;

    // In-memory storage for rate limiting
    // Key: IP/User/Global identifier
    // Value: Request count data
    private final Map<String, RequestData> requestCounts = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimited)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {

        // Skip rate limiting if disabled
        if (!rateLimitEnabled) {
            return joinPoint.proceed();
        }

        String key = generateKey(rateLimited.keyType());
        int limit = rateLimited.limit();
        long durationInMillis = TimeUnit.SECONDS.toMillis(rateLimited.duration());

        // Clean up expired entries
        cleanupExpiredEntries();

        // Get or create request data
        RequestData requestData = requestCounts.computeIfAbsent(key, k -> new RequestData());

        synchronized (requestData) {
            long currentTime = System.currentTimeMillis();

            // Reset if time window has passed
            if (currentTime - requestData.startTime > durationInMillis) {
                requestData.reset(currentTime);
            }

            // Check if limit exceeded
            if (requestData.count >= limit) {
                long remainingTime = durationInMillis - (currentTime - requestData.startTime);
                long remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(remainingTime);

                log.warn("Rate limit exceeded for key: {} - Limit: {}/{} seconds",
                        key, limit, rateLimited.duration());

                throw new CustomException(
                        rateLimited.message() + " Please try again in " + remainingSeconds + " seconds.",
                        "RATE_LIMIT_EXCEEDED"
                );
            }

            // Increment request count
            requestData.count++;
        }

        // Proceed with method execution
        return joinPoint.proceed();
    }

    /**
     * Generate rate limit key based on key type
     */
    private String generateKey(RateLimited.KeyType keyType) {
        switch (keyType) {
            case IP:
                return "ip:" + getClientIp();

            case USER:
                String userEmail = securityContextHelper.getCurrentUserEmail();
                return userEmail != null ? "user:" + userEmail : "ip:" + getClientIp();

            case GLOBAL:
                return "global:all";

            default:
                return "ip:" + getClientIp();
        }
    }

    /**
     * Get client IP address from request
     */
    private String getClientIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Forwarded-For");

            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }

            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }

            // If multiple IPs, take the first one
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }

            return ip;
        }

        return "unknown";
    }

    /**
     * Clean up expired entries from the map
     * Runs periodically to prevent memory leaks
     */
    private void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        long expiryThreshold = TimeUnit.MINUTES.toMillis(5); // Clean entries older than 5 minutes

        requestCounts.entrySet().removeIf(entry -> {
            RequestData data = entry.getValue();
            return (currentTime - data.startTime) > expiryThreshold;
        });
    }

    /**
     * Inner class to store request count data
     */
    private static class RequestData {
        private int count = 0;
        private long startTime = System.currentTimeMillis();

        public void reset(long currentTime) {
            this.count = 0;
            this.startTime = currentTime;
        }
    }
}