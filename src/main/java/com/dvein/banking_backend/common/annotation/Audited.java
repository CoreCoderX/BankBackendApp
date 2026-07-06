package com.dvein.banking_backend.common.annotation;

import com.dvein.banking_backend.common.enums.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    AuditAction action();
    String entityType() default "";
    String description() default "";
}