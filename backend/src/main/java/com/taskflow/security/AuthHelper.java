package com.taskflow.security;

import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthHelper {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Returns the UUID of the currently authenticated user.
     * Resolved from the Spring SecurityContext principal (email).
     */
    public UUID currentUserId() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB"))
                .getId();
    }
}
