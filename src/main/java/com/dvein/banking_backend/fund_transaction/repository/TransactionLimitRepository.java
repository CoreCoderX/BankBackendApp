package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.TransactionLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TransactionLimitRepository extends JpaRepository<TransactionLimit, Long> {

    Optional<TransactionLimit> findByUserIdAndCurrentDate(Long userId, LocalDate limitDate);

    @Query("SELECT tl FROM TransactionLimit tl WHERE tl.user.id = :userId AND tl.currentMonth = :month AND tl.currentYear = :year")
    Optional<TransactionLimit> findByUserIdAndMonth(
            @Param("userId") Long userId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );
}