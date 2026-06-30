package com.dvein.banking_backend.auth.repository;

import com.dvein.banking_backend.auth.model.TotpSecret;
import com.dvein.banking_backend.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TotpSecretRepository extends JpaRepository<TotpSecret, Long> {

    Optional<TotpSecret> findByUser(User user);

    Optional<TotpSecret> findByUserId(Long userId);

    boolean existsByUser(User user);
}