package com.dvein.banking_backend.account.repository;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Nominee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NomineeRepository extends JpaRepository<Nominee, Long> {

    List<Nominee> findByAccount(Account account);

    List<Nominee> findByAccountAndActiveTrue(Account account);

    long countByAccount(Account account);
}