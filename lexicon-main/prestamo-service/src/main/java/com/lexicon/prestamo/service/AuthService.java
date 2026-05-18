package com.lexicon.prestamo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.lexicon.prestamo.dto.AuthResponse;
import com.lexicon.prestamo.dto.LoginRequest;
import com.lexicon.prestamo.dto.RegisterRequest;
import com.lexicon.prestamo.entity.User;
import com.lexicon.prestamo.exception.AuthenticationException;
import com.lexicon.prestamo.exception.ValidationException;
import com.lexicon.prestamo.repository.UserRepository;
import com.lexicon.prestamo.util.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new ValidationException("Username already exists");
        }

        if (request.username() == null || request.username().isBlank()) {
            throw new ValidationException("Username cannot be empty");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new ValidationException("Password cannot be empty");
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = User.builder()
                .username(request.username())
                .passwordHash(passwordHash)
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());

        return new AuthResponse(token, jwtUtil.getExpiration(), user.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        if (request.username() == null || request.username().isBlank()) {
            throw new ValidationException("Username cannot be empty");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new ValidationException("Password cannot be empty");
        }

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        return new AuthResponse(token, jwtUtil.getExpiration(), user.getUsername());
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
}
