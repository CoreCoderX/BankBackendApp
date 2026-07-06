package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.MerchantCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantCategoryRepository extends JpaRepository<MerchantCategory, Long> {

    Optional<MerchantCategory> findByName(String name);

    boolean existsByName(String name);
}