package com.dvein.banking_backend.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for rate limiting endpoints
 * Apply this to controller methods to enforce rate limits
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    /**
     * Maximum number of requests allowed per time window
     */
    int limit() default 60;

    /**
     * Time window in seconds
     */
    int duration() default 60;

    /**
     * Key type for rate limiting
     * IP - Rate limit by IP address
     * USER - Rate limit by authenticated user
     * GLOBAL - Global rate limit
     */
    KeyType keyType() default KeyType.IP;

    /**
     * Custom error message when rate limit is exceeded
     */
    String message() default "Rate limit exceeded. Please try again later.";

    enum KeyType {
        IP,      // Rate limit by IP address
        USER,    // Rate limit by user ID
        GLOBAL   // Global rate limit
    }
}