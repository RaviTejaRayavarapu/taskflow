package com.taskflow.service;

import com.taskflow.dto.request.LoginRequest;
import com.taskflow.dto.request.RegisterRequest;
import com.taskflow.dto.response.AuthResponse;
import com.taskflow.dto.response.UserSummary;
import com.taskflow.exception.ConflictException;
import com.taskflow.model.User;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("email already in use");
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail().toLowerCase())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();

        User saved = userRepository.saveAndFlush(user);
        log.info("Registered new user: {}", saved.getEmail());

        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail());
        return AuthResponse.builder()
                .token(token)
                .user(UserSummary.from(saved))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        log.info("Login attempt for email: {}", req.getEmail());
        User user = userRepository.findByEmail(req.getEmail().toLowerCase())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", req.getEmail());
                    return new BadCredentialsException("invalid credentials");
                });

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            log.warn("Password mismatch for user: {}", req.getEmail());
            throw new BadCredentialsException("invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        log.info("Login successful for: {}", req.getEmail());
        return AuthResponse.builder()
                .token(token)
                .user(UserSummary.from(user))
                .build();
    }
}
