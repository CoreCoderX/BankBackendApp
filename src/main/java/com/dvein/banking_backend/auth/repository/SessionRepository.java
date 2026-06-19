package com.dvein.banking_backend.auth.repository;

import com.dvein.banking_backend.auth.model.Session;
import com.dvein.banking_backend.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByRefreshToken(String refreshToken);

    List<Session> findByUserAndActiveTrue(User user);

    List<Session> findByUser(User user);

    long countByUserAndActiveTrue(User user);

    @Modifying
    @Query("UPDATE Session s SET s.active = false WHERE s.user = :user")
    void invalidateAllUserSessions(User user);

    @Modifying
    @Query("UPDATE Session s SET s.active = false WHERE s.user = :user AND s.id != :currentSessionId")
    void invalidateOtherUserSessions(User user, Long currentSessionId);

    @Modifying
    @Query("DELETE FROM Session s WHERE s.expiresAt < :now OR s.active = false")
    void deleteExpiredAndInactiveSessions(LocalDateTime now);
}