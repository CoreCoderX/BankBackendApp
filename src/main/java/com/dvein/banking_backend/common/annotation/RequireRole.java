package com.dvein.banking_backend.common.annotation;

import com.dvein.banking_backend.common.enums.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for role-based access control
 * Apply this to controller methods or classes to enforce role requirements
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * Required roles to access this endpoint
     * User must have at least one of these roles
     */
    UserRole[] value();

    /**
     * Whether all roles are required (AND logic)
     * Default is false (OR logic - any one role is sufficient)
     */
    boolean requireAll() default false;

    /**
     * Custom error message when access is denied
     */
    String message() default "Access denied. Insufficient permissions.";
}