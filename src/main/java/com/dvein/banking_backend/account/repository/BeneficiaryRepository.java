package com.dvein.banking_backend.account.repository;

import com.dvein.banking_backend.account.model.Beneficiary;
import com.dvein.banking_backend.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByAccount(Account account);

    Optional<Beneficiary> findByAccountAndBeneficiaryAccountNumber(Account account, String accountNumber);

    long countByAccount(Account account);

    boolean existsByAccountAndBeneficiaryAccountNumber(Account account, String accountNumber);
}