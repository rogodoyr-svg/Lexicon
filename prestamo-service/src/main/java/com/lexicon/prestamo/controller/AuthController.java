package com.lexicon.prestamo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lexicon.prestamo.dto.ApiResponse;
import com.lexicon.prestamo.dto.AuthResponse;
import com.lexicon.prestamo.dto.LoginRequest;
import com.lexicon.prestamo.dto.RegisterRequest;
import com.lexicon.prestamo.exception.ValidationException;
import com.lexicon.prestamo.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        ApiResponse<AuthResponse> response = new ApiResponse<>(true, authResponse);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        ApiResponse<AuthResponse> response = new ApiResponse<>(true, authResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validate(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ValidationException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        boolean isValid = authService.validateToken(token);

        ApiResponse<Boolean> response = new ApiResponse<>(true, isValid);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AuthResponse>>> getAllUsers() {
        List<AuthResponse> usersWithTokens = authService.getAllUsersWithTokens();
        ApiResponse<List<AuthResponse>> response = new ApiResponse<>(true, usersWithTokens);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String username) {
        authService.deleteUser(username);
        ApiResponse<String> response = new ApiResponse<>(true, "Usuario eliminado con exito");
        return ResponseEntity.ok(response);
    }
}
