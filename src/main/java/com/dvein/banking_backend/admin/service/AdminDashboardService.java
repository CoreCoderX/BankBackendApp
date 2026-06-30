package com.dvein.banking_backend.admin.service;

import com.dvein.banking_backend.account.repository.AccountRepository;
import com.dvein.banking_backend.account.repository.CustomerRepository;
import com.dvein.banking_backend.account.repository.KycRepository;
import com.dvein.banking_backend.admin.dto.response.AdminDashboardResponse;
import com.dvein.banking_backend.auth.repository.SessionRepository;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.card.repository.CreditCardRepository;
import com.dvein.banking_backend.card.repository.DebitCardRepository;
import com.dvein.banking_backend.common.enums.CustomerStatus;
import com.dvein.banking_backend.common.enums.KycStatus;
import com.dvein.banking_backend.common.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final KycRepository kycRepository;
    private final DebitCardRepository debitCardRepository;
    private final CreditCardRepository creditCardRepository;
    private final SessionRepository sessionRepository;

    public AdminDashboardResponse getDashboardStats() {
        long totalCustomers    = userRepository.countByRole(UserRole.CUSTOMER);
        long activeCustomers   = customerRepository.countByStatus(CustomerStatus.ACTIVE);
        long blockedCustomers  = customerRepository.countByStatus(CustomerStatus.BLOCKED);
        long suspendedCustomers = customerRepository.countByStatus(CustomerStatus.SUSPENDED);

        long totalAdmins  = userRepository.countByRole(UserRole.ADMIN);
        long activeAdmins = userRepository.countByActiveAndRole(true, UserRole.ADMIN);

        long totalAccounts = accountRepository.count();

        // FIX: Use aggregate SQL SUM — avoids loading all Account rows into memory
        BigDecimal totalBalance = accountRepository.sumTotalBalance();

        long pendingKyc = kycRepository.countByStatus(KycStatus.SUBMITTED);

        // FIX: Count only cards where approved=false AND rejectionReason IS NULL (true pending)
        long pendingCreditCards = creditCardRepository.countByApprovedFalseAndRejectionReasonIsNull();

        long totalDebitCards  = debitCardRepository.count();
        long totalCreditCards = creditCardRepository.count();

        // FIX: Use aggregate DB query — avoids loading all Session rows into memory
        long activeSessions = sessionRepository.countActiveSessions(LocalDateTime.now());

        return AdminDashboardResponse.builder()
                .totalCustomers(totalCustomers)
                .activeCustomers(activeCustomers)
                .blockedCustomers(blockedCustomers)
                .suspendedCustomers(suspendedCustomers)
                .totalAdmins(totalAdmins)
                .activeAdmins(activeAdmins)
                .totalAccounts(totalAccounts)
                .totalBalance(totalBalance)
                .pendingKycApprovals(pendingKyc)
                .pendingCreditCardApplications(pendingCreditCards)
                .totalDebitCards(totalDebitCards)
                .totalCreditCards(totalCreditCards)
                .failedLoginAttemptsToday(0L)  // TODO: implement audit-based counter
                .newRegistrationsToday(0L)     // TODO: implement audit-based counter
                .activeSessions(activeSessions)
                .build();
    }
}