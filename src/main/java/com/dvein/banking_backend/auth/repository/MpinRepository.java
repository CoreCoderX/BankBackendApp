package com.dvein.banking_backend.auth.repository;

import com.dvein.banking_backend.auth.model.Mpin;
import com.dvein.banking_backend.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MpinRepository extends JpaRepository<Mpin, Long> {

    Optional<Mpin> findByUser(User user);

    Optional<Mpin> findByUserId(Long userId);

    boolean existsByUser(User user);
}