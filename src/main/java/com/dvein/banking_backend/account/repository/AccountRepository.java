package com.dvein.banking_backend.account.repository;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.common.enums.AccountStatus;
import com.dvein.banking_backend.common.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomer(Customer customer);

    List<Account> findByCustomerAndStatus(Customer customer, AccountStatus status);

    List<Account> findByCustomerAndAccountType(Customer customer, AccountType accountType);

    Optional<Account> findByCustomerAndPrimaryTrue(Customer customer);

    Optional<Account> findByIdAndCustomerUserEmail(
            Long accountId,
            String email);

    long countByCustomer(Customer customer);

    boolean existsByAccountNumber(String accountNumber);
}