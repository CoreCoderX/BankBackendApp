package com.dvein.banking_backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableAsync
@EnableJpaAuditing
@EnableScheduling
public class BankingBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(BankingBackendApplication.class, args);
		System.out.println("API Swagger UI Documentation: http://localhost:8080/api/v1/swagger-ui/index.html");
		System.out.println("API JSON Documentation http://localhost:8080/api/v1/api-docs");
	}

}
