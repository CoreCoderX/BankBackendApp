package com.dvein.banking_backend.account.repository;

import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.model.Kyc;
import com.dvein.banking_backend.common.enums.KycStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycRepository extends JpaRepository<Kyc, Long> {

    Optional<Kyc> findByCustomer(Customer customer);

    Page<Kyc> findByStatus(KycStatus status, Pageable pageable);

    long countByStatus(KycStatus status);
}