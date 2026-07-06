package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.transaction.model.UpiProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UpiProfileRepository extends JpaRepository<UpiProfile, Long> {

    Optional<UpiProfile> findByCustomer(Customer customer);

    Optional<UpiProfile> findByCustomerId(Long customerId);

    @Query("SELECT up FROM UpiProfile up WHERE up.customer.user.email = :email")
    Optional<UpiProfile> findByCustomerUserEmail(@Param("email") String email);

    boolean existsByCustomer(Customer customer);
}