package com.dvein.banking_backend.auth.repository;

import com.dvein.banking_backend.auth.model.Otp;
import com.dvein.banking_backend.common.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findByEmailAndOtpTypeAndVerifiedFalse(String email, OtpType otpType);

    Optional<Otp> findTopByEmailAndOtpTypeOrderByCreatedAtDesc(String email, OtpType otpType);

    @Modifying
    @Query("DELETE FROM Otp o WHERE o.expiresAt < :now OR o.verified = true")
    void deleteExpiredAndVerified(LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Otp o WHERE o.email = :email AND o.otpType = :otpType")
    void deleteByEmailAndOtpType(String email, OtpType otpType);
}