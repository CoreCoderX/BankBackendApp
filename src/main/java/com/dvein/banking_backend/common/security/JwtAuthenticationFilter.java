package com.dvein.banking_backend.common.security;

import com.dvein.banking_backend.auth.service.TokenBlacklistService;  // ADD THIS LINE
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;  // ADD THIS LINE

    private static final List<String> PRE_AUTH_ALLOWED_ENDPOINTS = Arrays.asList(
            "/auth/verify-device",
            "/auth/verify-totp",
            "/totp/verify"
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            // ADD THIS BLOCK (3 lines) ⬇️
            if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Token revoked. Please login again.\"}");
                return;
            }
            // END OF ADDITION ⬆️

            userEmail = jwtTokenProvider.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtTokenProvider.isTokenValid(jwt, userEmail)) {

                    if (jwtTokenProvider.isPreAuthToken(jwt)) {
                        if (isPreAuthAllowedEndpoint(request.getRequestURI())) {
                            setAuthentication(userDetails, request);
                        } else {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"success\":false,\"message\":\"MFA required\"}");
                            return;
                        }
                    } else if (jwtTokenProvider.isAccessToken(jwt)) {
                        setAuthentication(userDetails, request);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private boolean isPreAuthAllowedEndpoint(String requestURI) {
        return PRE_AUTH_ALLOWED_ENDPOINTS.stream().anyMatch(requestURI::contains);
    }
}