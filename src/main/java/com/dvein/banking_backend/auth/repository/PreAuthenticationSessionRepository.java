package com.dvein.banking_backend.auth.repository;

import com.dvein.banking_backend.auth.model.PreAuthenticationSession;
import com.dvein.banking_backend.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PreAuthenticationSessionRepository extends JpaRepository<PreAuthenticationSession, Long> {

    Optional<PreAuthenticationSession> findByPreAuthToken(String preAuthToken);

    Optional<PreAuthenticationSession> findByUserAndActiveTrue(User user);

    @Modifying
    @Query("UPDATE PreAuthenticationSession p SET p.active = false WHERE p.user = :user")
    void deactivateAllByUser(User user);

    @Modifying
    @Query("UPDATE PreAuthenticationSession p SET p.active = false WHERE p.expiresAt < :now")
    void deactivateExpiredSessions(LocalDateTime now);

    void deleteByUserAndActiveFalse(User user);
}