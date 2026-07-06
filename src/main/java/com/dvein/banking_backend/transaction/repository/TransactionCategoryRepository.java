package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Long> {

    Optional<TransactionCategory> findByName(String name);

    boolean existsByName(String name);
}