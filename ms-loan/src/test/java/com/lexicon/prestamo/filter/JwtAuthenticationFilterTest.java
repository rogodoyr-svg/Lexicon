package com.lexicon.prestamo.filter;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.lexicon.prestamo.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtUtil);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should extract and validate valid token from header")
    void testValidTokenInHeader() throws ServletException, IOException {
        String token = "valid-jwt-token";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsername(token)).thenReturn("testuser");

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil, times(1)).validateToken(token);
        verify(jwtUtil, times(1)).getUsername(token);
        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testuser", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    @DisplayName("Should set SecurityContext with valid token")
    void testSecurityContextSetWithValidToken() throws ServletException, IOException {
        String token = "valid-token";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsername(token)).thenReturn("alice");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("alice", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    @DisplayName("Should continue filter chain when Authorization header is missing")
    void testMissingAuthorizationHeader() throws ServletException, IOException {
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should not set SecurityContext for invalid token")
    void testInvalidToken() throws ServletException, IOException {
        String token = "invalid-token";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil, times(1)).validateToken(token);
        verify(jwtUtil, never()).getUsername(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should not process token without Bearer prefix")
    void testWithoutBearerPrefix() throws ServletException, IOException {
        request.addHeader("Authorization", "my-jwt-token");
        filter.doFilterInternal(request, response, filterChain);
        verify(jwtUtil, never()).validateToken(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle exception during token validation")
    void testExceptionDuringValidation() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer token123");
        when(jwtUtil.validateToken("token123")).thenThrow(new RuntimeException("JWT error"));
        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should always call filter chain doFilter")
    void testFilterChainAlwaysCalled() throws ServletException, IOException {
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
