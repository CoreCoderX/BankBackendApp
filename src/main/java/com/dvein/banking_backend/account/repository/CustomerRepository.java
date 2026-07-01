package com.dvein.banking_backend.account.repository;

import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.common.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUserId(Long userId);

    Optional<Customer> findByUserEmail(String email);

    Optional<Customer> findByUser(User user);

    Optional<Customer> findByPan(String pan);

    Optional<Customer> findByAadhaar(String aadhaar);

    boolean existsByPan(String pan);

    boolean existsByAadhaar(String aadhaar);

    Page<Customer> findByStatus(CustomerStatus status, Pageable pageable);

    Page<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    long countByStatus(CustomerStatus status);
}