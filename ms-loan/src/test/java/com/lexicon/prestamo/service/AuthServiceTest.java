package com.lexicon.prestamo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.lexicon.prestamo.dto.AuthResponse;
import com.lexicon.prestamo.dto.LoginRequest;
import com.lexicon.prestamo.dto.RegisterRequest;
import com.lexicon.prestamo.entity.User;
import com.lexicon.prestamo.exception.AuthenticationException;
import com.lexicon.prestamo.exception.ValidationException;
import com.lexicon.prestamo.repository.UserRepository;
import com.lexicon.prestamo.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest("testuser", "password123");
        validLoginRequest = new LoginRequest("testuser", "password123");
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .passwordHash("$2a$10$hashedpassword")
                .build();
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegisterSuccess() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken("testuser")).thenReturn("jwt-token-123");
        when(jwtUtil.getExpiration()).thenReturn(3600000L);

        AuthResponse response = authService.register(validRegisterRequest);

        assertNotNull(response);
        assertEquals("testuser", response.username());
        assertEquals("jwt-token-123", response.token());
        assertEquals(3600000L, response.expiresIn());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when username already exists")
    void testRegisterWithDuplicateUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        assertThrows(ValidationException.class, () -> authService.register(validRegisterRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when username is empty")
    void testRegisterWithEmptyUsername() {
        RegisterRequest invalidRequest = new RegisterRequest("", "password123");
        assertThrows(ValidationException.class, () -> authService.register(invalidRequest));
    }

    @Test
    @DisplayName("Should throw ValidationException when username is null")
    void testRegisterWithNullUsername() {
        RegisterRequest invalidRequest = new RegisterRequest(null, "password123");
        assertThrows(ValidationException.class, () -> authService.register(invalidRequest));
    }

    @Test
    @DisplayName("Should throw ValidationException when password is empty")
    void testRegisterWithEmptyPassword() {
        RegisterRequest invalidRequest = new RegisterRequest("testuser", "");
        assertThrows(ValidationException.class, () -> authService.register(invalidRequest));
    }

    @Test
    @DisplayName("Should throw ValidationException when password is null")
    void testRegisterWithNullPassword() {
        RegisterRequest invalidRequest = new RegisterRequest("testuser", null);
        assertThrows(ValidationException.class, () -> authService.register(invalidRequest));
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLoginSuccess() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedpassword")).thenReturn(true);
        when(jwtUtil.generateToken("testuser")).thenReturn("jwt-token-456");
        when(jwtUtil.getExpiration()).thenReturn(3600000L);

        AuthResponse response = authService.login(validLoginRequest);

        assertNotNull(response);
        assertEquals("testuser", response.username());
        assertEquals("jwt-token-456", response.token());
        assertEquals(3600000L, response.expiresIn());
    }

    @Test
    @DisplayName("Should throw AuthenticationException when user not found")
    void testLoginWithNonExistentUser() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        assertThrows(AuthenticationException.class, () -> authService.login(new LoginRequest("nonexistent", "password123")));
    }

    @Test
    @DisplayName("Should throw AuthenticationException when password is incorrect")
    void testLoginWithWrongPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedpassword")).thenReturn(false);
        assertThrows(AuthenticationException.class, () -> authService.login(new LoginRequest("testuser", "wrongpassword")));
    }

    @Test
    @DisplayName("Should throw ValidationException when login username is empty")
    void testLoginWithEmptyUsername() {
        LoginRequest invalidRequest = new LoginRequest("", "password123");
        assertThrows(ValidationException.class, () -> authService.login(invalidRequest));
    }

    @Test
    @DisplayName("Should throw ValidationException when login password is empty")
    void testLoginWithEmptyPassword() {
        LoginRequest invalidRequest = new LoginRequest("testuser", "");
        assertThrows(ValidationException.class, () -> authService.login(invalidRequest));
    }

    @Test
    @DisplayName("Should return true for valid token")
    void testValidateValidToken() {
        when(jwtUtil.validateToken("valid-jwt-token")).thenReturn(true);
        assertTrue(authService.validateToken("valid-jwt-token"));
    }

    @Test
    @DisplayName("Should return false for invalid token")
    void testValidateInvalidToken() {
        when(jwtUtil.validateToken("invalid-jwt-token")).thenReturn(false);
        assertFalse(authService.validateToken("invalid-jwt-token"));
    }
}
