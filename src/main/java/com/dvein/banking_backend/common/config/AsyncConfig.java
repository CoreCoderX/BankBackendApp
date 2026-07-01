// common/config/AsyncConfig.java
package com.dvein.banking_backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Default async executor for general tasks
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("banking-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * Dedicated executor for report generation
     * Reports are I/O intensive, so we use more threads
     */
    @Bean(name = "reportTaskExecutor")
    public Executor reportTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);              // Minimum threads for reports
        executor.setMaxPoolSize(5);               // Maximum threads for reports
        executor.setQueueCapacity(100);           // Queue for pending report tasks
        executor.setThreadNamePrefix("report-");  // Thread name for debugging
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120); // Wait longer for report completion
        executor.initialize();
        return executor;
    }

    /**
     * Dedicated executor for email tasks
     * Emails should be sent quickly without blocking reports
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("email-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();  // ✅ FIXED: Added executor. prefix
        return executor;
    }

    /**
     * Dedicated executor for notification tasks
     */
    @Bean(name = "notificationTaskExecutor")
    public Executor notificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("notification-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();  // ✅ Already correct
        return executor;
    }

    /**
     * Handle uncaught exceptions in async methods
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            StringBuilder message = new StringBuilder();
            message.append("Uncaught exception in async method: ");
            message.append(method.getDeclaringClass().getSimpleName());
            message.append(".").append(method.getName()).append("(");

            for (int i = 0; i < params.length; i++) {
                if (i > 0) message.append(", ");
                message.append(params[i]);
            }
            message.append(")");

            System.err.println(message);
            ex.printStackTrace();
        };
    }
}