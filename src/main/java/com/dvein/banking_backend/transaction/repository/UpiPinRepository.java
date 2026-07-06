package com.dvein.banking_backend.transaction.repository;

import com.dvein.banking_backend.transaction.model.UpiPin;
import com.dvein.banking_backend.transaction.model.UpiProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UpiPinRepository extends JpaRepository<UpiPin, Long> {

    Optional<UpiPin> findByUpiProfile(UpiProfile upiProfile);

    boolean existsByUpiProfile(UpiProfile upiProfile);
}