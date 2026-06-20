package com.dvein.banking_backend.loan.repository;

import com.dvein.banking_backend.loan.enums.RepaymentStatus;
import com.dvein.banking_backend.loan.model.LoanSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanScheduleRepository extends JpaRepository<LoanSchedule, Long> {

    List<LoanSchedule> findByLoanIdOrderByEmiNumberAsc(Long loanId);

    @Query("SELECT s FROM LoanSchedule s WHERE s.loan.id = :loanId AND s.status = :status ORDER BY s.emiNumber ASC")
    List<LoanSchedule> findByLoanIdAndStatus(
            @Param("loanId") Long loanId,
            @Param("status") RepaymentStatus status
    );

    @Query("SELECT s FROM LoanSchedule s WHERE s.loan.id = :loanId AND s.status = 'PENDING' ORDER BY s.emiNumber ASC")
    Optional<LoanSchedule> findNextPendingEmi(@Param("loanId") Long loanId);

    @Query("SELECT COUNT(s) FROM LoanSchedule s WHERE s.loan.id = :loanId AND s.status = 'PAID'")
    Long countPaidEmis(@Param("loanId") Long loanId);

    @Query("SELECT COUNT(s) FROM LoanSchedule s WHERE s.loan.id = :loanId AND s.status = 'PENDING'")
    Long countPendingEmis(@Param("loanId") Long loanId);

    @Query("SELECT s FROM LoanSchedule s WHERE s.dueDate < :date AND s.status = 'PENDING'")
    List<LoanSchedule> findOverdueSchedules(@Param("date") LocalDate date);
}