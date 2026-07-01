package com.dvein.banking_backend.account.repository;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.common.enums.AccountStatus;
import com.dvein.banking_backend.common.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findAllByCustomerId(Long customerId);

    Optional<Account> findByCustomerIdAndPrimaryTrue(Long customerId);

    List<Account> findByCustomer(Customer customer);

    List<Account> findByCustomerAndStatus(Customer customer, AccountStatus status);

    List<Account> findByCustomerAndAccountType(Customer customer, AccountType accountType);

    Optional<Account> findByIdAndCustomerUserEmail(Long id, String email);

    Optional<Account> findByCustomerAndPrimaryTrue(Customer customer);

    long countByCustomer(Customer customer);

    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a")
    BigDecimal sumTotalBalance();
}