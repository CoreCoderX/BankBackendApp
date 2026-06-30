package com.dvein.banking_backend.loan.repository;

import com.dvein.banking_backend.loan.enums.LoanStatus;
import com.dvein.banking_backend.loan.model.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    Optional<Loan> findByLoanNumber(String loanNumber);

    Optional<Loan> findByIdAndUserId(Long id, Long userId);

    Page<Loan> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Loan> findByUserIdAndStatus(Long userId, LoanStatus status);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.user.id = :userId AND l.status IN ('ACTIVE', 'DISBURSED', 'OVERDUE')")
    Long countActiveLoansForUser(@Param("userId") Long userId);

    @Query("SELECT l FROM Loan l WHERE l.status = :status")
    Page<Loan> findByStatus(@Param("status") LoanStatus status, Pageable pageable);

    @Query("SELECT l FROM Loan l WHERE l.status IN ('ACTIVE', 'DISBURSED') ")
    List<Loan> findActiveLoans();

    @Query("SELECT SUM(l.remainingPrincipal) FROM Loan l WHERE l.user.id = :userId AND l.status IN ('ACTIVE', 'DISBURSED', 'OVERDUE')")
    java.math.BigDecimal getTotalOutstandingForUser(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {
            "account",
            "account.customer",
            "user"
    })
    Optional<Loan> findWithDetailsById(Long id);
}