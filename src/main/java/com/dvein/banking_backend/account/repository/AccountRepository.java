package com.dvein.banking_backend.account.repository;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.common.enums.AccountStatus;
import com.dvein.banking_backend.common.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomer(Customer customer);

    List<Account> findByCustomerAndStatus(Customer customer, AccountStatus status);

    List<Account> findByCustomerAndAccountType(Customer customer, AccountType accountType);

    Optional<Account> findByCustomerAndPrimaryTrue(Customer customer);

    Optional<Account> findByIdAndCustomerUserEmail(Long accountId, String email);

    /** IDOR-safe: fetch only if the account belongs to the given userId */
    Optional<Account> findByIdAndCustomerUserId(Long accountId, Long userId);

    long countByCustomer(Customer customer);

    boolean existsByAccountNumber(String accountNumber);

    /** Aggregate total balance — avoids loading all rows into memory */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a")
    BigDecimal sumTotalBalance();

    /** Count active sessions via aggregate — for dashboard */
    @Query("SELECT COUNT(a) FROM Account a WHERE a.status = 'ACTIVE'")
    long countActiveAccounts();
}