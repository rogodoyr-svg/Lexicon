package com.lexicon.auth.service;


import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.lexicon.auth.dto.AuthResponse;
import com.lexicon.auth.dto.LoginRequest;
import com.lexicon.auth.dto.RegisterRequest;
import com.lexicon.auth.model.User;
import com.lexicon.auth.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmailAndActiveTrue(request.email())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
		}

		return new AuthResponse(jwtService.generateToken(user.getEmail()));
	}

    public AuthResponse register(RegisterRequest request) {
		if (userRepository.findByEmailAndActiveTrue(request.email()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
		}

		User user = new User(request.email(), passwordEncoder.encode(request.password()));
		userRepository.save(user);

		return new AuthResponse(jwtService.generateToken(user.getEmail()));
	}

    
}
