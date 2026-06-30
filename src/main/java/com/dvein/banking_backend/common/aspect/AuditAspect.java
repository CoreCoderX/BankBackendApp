package com.dvein.banking_backend.common.aspect;

import com.dvein.banking_backend.admin.service.AuditService;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.annotation.Audited;
import com.dvein.banking_backend.common.security.SecurityContextHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Aspect for automatic audit logging
 * Intercepts methods annotated with @Audited and logs the action
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;

    /**
     * Log successful audited actions
     */
    @AfterReturning(pointcut = "@annotation(audited)", returning = "result")
    public void logSuccessfulAuditedAction(JoinPoint joinPoint, Audited audited, Object result) {
        try {
            logAuditAction(joinPoint, audited, true, null);
        } catch (Exception e) {
            log.error("Failed to create audit log for successful action", e);
        }
    }

    /**
     * Log failed audited actions
     */
    @AfterThrowing(pointcut = "@annotation(audited)", throwing = "exception")
    public void logFailedAuditedAction(JoinPoint joinPoint, Audited audited, Throwable exception) {
        try {
            logAuditAction(joinPoint, audited, false, exception.getMessage());
        } catch (Exception e) {
            log.error("Failed to create audit log for failed action", e);
        }
    }

    /**
     * Common method to log audit actions
     */
    private void logAuditAction(JoinPoint joinPoint, Audited audited, boolean success, String errorMessage) {
        String currentUserEmail = securityContextHelper.getCurrentUserEmail();

        if (currentUserEmail == null) {
            log.debug("No authenticated user found for audit logging");
            return;
        }

        // Get current user
        User user = userRepository.findByEmail(currentUserEmail).orElse(null);

        if (user == null) {
            log.debug("User not found for audit logging: {}", currentUserEmail);
            return;
        }

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // Build description
            String description = success ? audited.description() :
                    audited.description() + " (Failed: " + errorMessage + ")";

            // Extract entity ID from method arguments if available
            Long entityId = extractEntityId(joinPoint);

            // Log the audit
            auditService.log(
                    user.getId(),
                    audited.action(),
                    audited.entityType(),
                    entityId,
                    description,
                    request
            );

            log.debug("Audit log created: {} - {} - {}",
                    user.getEmail(), audited.action(), description);
        }
    }

    /**
     * Extract entity ID from method arguments
     * Looks for the first Long parameter as entity ID
     */
    private Long extractEntityId(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        if (args != null && args.length > 0) {
            for (Object arg : args) {
                if (arg instanceof Long) {
                    return (Long) arg;
                }
            }
        }

        return null;
    }
}