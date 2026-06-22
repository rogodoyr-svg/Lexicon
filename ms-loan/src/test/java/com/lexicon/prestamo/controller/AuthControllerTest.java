package com.lexicon.prestamo.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexicon.prestamo.dto.AuthResponse;
import com.lexicon.prestamo.dto.LoginRequest;
import com.lexicon.prestamo.dto.RegisterRequest;
import com.lexicon.prestamo.exception.AuthenticationException;
import com.lexicon.prestamo.exception.GlobalExceptionHandler;
import com.lexicon.prestamo.exception.ValidationException;
import com.lexicon.prestamo.service.AuthService;
import com.lexicon.prestamo.util.JwtUtil;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest("testuser", "password123");
        validLoginRequest = new LoginRequest("testuser", "password123");
        authResponse = new AuthResponse("jwt-token-123", 3600000L, "testuser");
    }

    @Test
    @DisplayName("POST /api/auth/register - Should register new user and return 201")
    void testRegisterSuccess() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", equalTo("testuser")))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.expiresIn", is(3600000)));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 for duplicate username")
    void testRegisterDuplicateUsername() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ValidationException("Username already exists"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", equalTo("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should login and return 200 with token")
    void testLoginSuccess() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", equalTo("testuser")))
                .andExpect(jsonPath("$.data.token", equalTo("jwt-token-123")))
                .andExpect(jsonPath("$.data.expiresIn", is(3600000)));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 401 for invalid credentials")
    void testLoginInvalidCredentials() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", equalTo("AUTHENTICATION_ERROR")));
    }

    @Test
    @DisplayName("GET /api/auth/validate - Should return true for valid token")
    void testValidateValidToken() throws Exception {
        when(authService.validateToken("valid-token")).thenReturn(true);

        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(true)));
    }

    @Test
    @DisplayName("GET /api/auth/validate - Should return 400 for missing Authorization header")
    void testValidateMissingHeader() throws Exception {
        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", equalTo("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("GET /api/auth/validate - Should return 400 for missing Bearer prefix")
    void testValidateMissingBearerPrefix() throws Exception {
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", equalTo("VALIDATION_ERROR")));
    }
}
