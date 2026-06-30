package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.Merchant;
import com.dvein.banking_backend.transaction.model.MerchantCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Optional<Merchant> findByMerchantCode(String merchantCode);

    Optional<Merchant> findByUpiId(String upiId);

    List<Merchant> findByCategory(MerchantCategory category);

    Page<Merchant> findByActiveTrue(Pageable pageable);

    Page<Merchant> findByActiveTrueAndVerifiedTrue(Pageable pageable);

    boolean existsByMerchantCode(String merchantCode);

    boolean existsByUpiId(String upiId);
}