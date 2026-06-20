package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByUserIdAndIsActiveTrue(Long userId);

    Optional<Beneficiary> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndAccountNumber(Long userId, String accountNumber);

    Optional<Beneficiary> findByUserIdAndAccountNumber(Long userId, String accountNumber);
}