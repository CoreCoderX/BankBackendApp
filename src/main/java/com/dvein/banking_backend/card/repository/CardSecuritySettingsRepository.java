package com.dvein.banking_backend.card.repository;

import com.dvein.banking_backend.card.model.CardSecuritySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardSecuritySettingsRepository extends JpaRepository<CardSecuritySettings, Long> {

    Optional<CardSecuritySettings> findByCardId(Long cardId);

    boolean existsByCardId(Long cardId);
}