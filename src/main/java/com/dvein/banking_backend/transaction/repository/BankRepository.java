package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {

    Optional<Bank> findByBankCode(String bankCode);

    Optional<Bank> findByIfscPrefix(String ifscPrefix);

    List<Bank> findByActiveTrue();

    boolean existsByBankCode(String bankCode);
}