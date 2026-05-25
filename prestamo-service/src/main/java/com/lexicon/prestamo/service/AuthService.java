package com.lexicon.prestamo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.lexicon.prestamo.dto.AuthResponse;
import com.lexicon.prestamo.dto.LoginRequest;
import com.lexicon.prestamo.dto.RegisterRequest;
import com.lexicon.prestamo.entity.User;
import com.lexicon.prestamo.exception.AuthenticationException;
import com.lexicon.prestamo.exception.ResourceNotFoundException;
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
            throw new ValidationException("El nombre de usuario ya existe");
        }

        if (request.username() == null || request.username().isBlank()) {
            throw new ValidationException("El nombre de usuario no puede estar vacío");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new ValidationException("La contraseña no puede estar vacía");
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
            throw new ValidationException("El nombre de usuario no puede estar vacío");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new ValidationException("La contraseña no puede estar vacía");
        }

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new AuthenticationException("Nombre de usuario o contraseña inválidos"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationException("Nombre de usuario o contraseña inválidos");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        return new AuthResponse(token, jwtUtil.getExpiration(), user.getUsername());
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public java.util.List<AuthResponse> getAllUsersWithTokens() {
        return userRepository.findAll().stream()
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getUsername());
                    return new AuthResponse(token, jwtUtil.getExpiration(), user.getUsername());
                })
                .toList();
    }

    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        userRepository.delete(user);
    }
}
