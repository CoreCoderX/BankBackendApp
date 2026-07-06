package com.dvein.banking_backend.common.security;

import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.repository.CustomerRepository;
import com.dvein.banking_backend.auth.model.User;
import com.dvein.banking_backend.auth.repository.UserRepository;
import com.dvein.banking_backend.common.enums.CustomerStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        if (user.getRole().name().equals("CUSTOMER")) {

            Customer customer = customerRepository.findByUser(user)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("Customer profile not found"));

            if (customer.getStatus() != CustomerStatus.ACTIVE) {
                throw new UsernameNotFoundException(
                        "Customer account is " + customer.getStatus());
            }
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                ))
                .accountExpired(false)
                .accountLocked(user.isLocked())
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }
}