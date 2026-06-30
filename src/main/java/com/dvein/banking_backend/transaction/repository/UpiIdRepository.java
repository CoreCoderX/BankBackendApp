package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.UpiId;
import com.dvein.banking_backend.transaction.model.UpiProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UpiIdRepository extends JpaRepository<UpiId, Long> {

    Optional<UpiId> findByUpiId(String upiId);

    List<UpiId> findByUpiProfile(UpiProfile upiProfile);

    List<UpiId> findByUpiProfileAndActiveTrue(UpiProfile upiProfile);

    Optional<UpiId> findByUpiProfileAndPrimaryTrue(UpiProfile upiProfile);

    boolean existsByUpiId(String upiId);

    @Query("SELECT u FROM UpiId u WHERE u.id = :upiIdId " +
            "AND u.upiProfile.customer.user.email = :email")
    Optional<UpiId> findByIdAndUserEmail(@Param("upiIdId") Long upiIdId,
                                         @Param("email") String email);
}