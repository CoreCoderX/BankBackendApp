package com.dvein.banking_backend.auth.repository;

import com.dvein.banking_backend.auth.model.LoginHistory;
import com.dvein.banking_backend.auth.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    Page<LoginHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<LoginHistory> findTop10ByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user = :user AND lh.createdAt > :since ORDER BY lh.createdAt DESC")
    List<LoginHistory> findRecentLoginsByUser(User user, LocalDateTime since);

    long countByUserAndSuccessfulFalseAndCreatedAtAfter(User user, LocalDateTime since);
}