package com.dvein.banking_backend.loan.repository;

import com.dvein.banking_backend.loan.model.LoanPenalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LoanPenaltyRepository extends JpaRepository<LoanPenalty, Long> {

    List<LoanPenalty> findByLoanId(Long loanId);

    @Query("SELECT SUM(p.amount) FROM LoanPenalty p WHERE p.loan.id = :loanId AND p.isPaid = false")
    BigDecimal getUnpaidPenaltyForLoan(@Param("loanId") Long loanId);

    List<LoanPenalty> findByLoanIdAndIsPaidFalse(Long loanId);
}