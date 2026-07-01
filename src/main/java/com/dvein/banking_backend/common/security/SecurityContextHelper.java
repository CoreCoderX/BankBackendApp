package com.dvein.banking_backend.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.dvein.banking_backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.dvein.banking_backend.common.enums.UserRole;
import com.dvein.banking_backend.account.repository.CustomerRepository;

@Component
@RequiredArgsConstructor
public class SecurityContextHelper {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }

        return null;
    }

    public String getCurrentUserEmailOrThrow() {
        String email = getCurrentUserEmail();

        if (email == null) {
            throw new IllegalStateException("No authenticated user found.");
        }

        return email;
    }

    public Long getCurrentUserId() {
        String email = getCurrentUserEmailOrThrow();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"))
                .getId();
    }

    public Long getCurrentCustomerId() {

        String email = getCurrentUserEmailOrThrow();

        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
                .getId();

        return customerRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Customer not found"))
                .getId();
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    public UserRole getUserRole() {
        String email = getCurrentUserEmailOrThrow();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
                .getRole();
    }


}