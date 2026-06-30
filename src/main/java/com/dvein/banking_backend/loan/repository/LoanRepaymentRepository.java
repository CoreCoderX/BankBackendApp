package com.dvein.banking_backend.loan.repository;

import com.dvein.banking_backend.loan.model.LoanRepayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {

    List<LoanRepayment> findByLoanIdOrderByPaymentDateDesc(Long loanId);

    Page<LoanRepayment> findByLoanIdOrderByPaymentDateDesc(Long loanId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(r) FROM LoanRepayment r WHERE r.loan.id = :loanId")
    Long countByLoanId(@org.springframework.data.repository.query.Param("loanId") Long loanId);
}