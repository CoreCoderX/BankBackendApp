package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.transaction.model.Biller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillerRepository extends JpaRepository<Biller, Long> {

    List<Biller> findByCustomerOrderByCreatedAtDesc(Customer customer);

    List<Biller> findByCustomerAndBillerCategory(Customer customer, String category);

    List<Biller> findByAutoPayEnabledTrue();

    @Query("SELECT b FROM Biller b WHERE b.id = :id " +
            "AND b.customer.user.email = :email")
    Optional<Biller> findByIdAndUserEmail(@Param("id") Long id,
                                          @Param("email") String email);
}