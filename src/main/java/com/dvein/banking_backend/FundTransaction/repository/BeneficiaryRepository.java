package com.dvein.banking_backend.FundTransaction.repository;

import com.dvein.banking_backend.FundTransaction.model.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("transactionBeneficiaryRepository") // Explicit name prevents collision with your account repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    // FIX: Changed Is_activeTrue to IsActiveTrue
    List<Beneficiary> findByUserIdAndIsActiveTrue(Long userId);

    // This one was already perfect!
    Optional<Beneficiary> findByIdAndUserId(Long id, Long userId);

    // FIX: Changed Account_number to AccountNumber
    boolean existsByUserIdAndAccountNumber(Long userId, String accountNumber);

    // FIX: Changed Account_number to AccountNumber
    Optional<Beneficiary> findByUserIdAndAccountNumber(Long userId, String accountNumber);
}