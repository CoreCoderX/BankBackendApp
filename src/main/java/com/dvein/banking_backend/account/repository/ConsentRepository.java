package com.dvein.banking_backend.account.repository;

import com.dvein.banking_backend.account.model.Consent;
import com.dvein.banking_backend.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, Long> {

    Optional<Consent> findByUserAndConsentType(User user, String consentType);

    List<Consent> findByUser(User user);

    List<Consent> findByUserAndAcceptedTrue(User user);

    boolean existsByUserAndConsentTypeAndAcceptedTrue(User user, String consentType);
}