package com.dvein.banking_backend.auth.repository;

import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.common.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Query("SELECT u FROM User u WHERE (u.email = :identifier OR u.phone = :identifier)")
    Optional<User> findByEmailOrPhone(String identifier);

    Optional<User> findByEmailAndRole(String email, UserRole role);

    long countByRole(UserRole role);

    long countByActiveAndRole(boolean active, UserRole role);

    long countByLockedAndRole(boolean locked, UserRole role);
}