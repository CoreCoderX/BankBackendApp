package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.account.model.Account;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.transaction.model.StandingInstruction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StandingInstructionRepository extends JpaRepository<StandingInstruction, Long> {

    List<StandingInstruction> findByCustomerOrderByCreatedAtDesc(Customer customer);

    List<StandingInstruction> findBySenderAccountOrderByCreatedAtDesc(Account account);

    @Query("SELECT si FROM StandingInstruction si WHERE " +
            "si.active = true AND si.paused = false " +
            "AND si.nextExecutionDate <= :date")
    List<StandingInstruction> findDueInstructions(@Param("date") LocalDate date);

    @Query("SELECT si FROM StandingInstruction si WHERE si.id = :id " +
            "AND si.customer.user.email = :email")
    Optional<StandingInstruction> findByIdAndUserEmail(@Param("id") Long id,
                                                       @Param("email") String email);
}