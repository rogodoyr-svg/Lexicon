package com.lexicon.prestamo.exception;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import com.lexicon.prestamo.dto.ApiResponse;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private ServletWebRequest mockRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        mockRequest = new ServletWebRequest(request);
    }

    @Test
    @DisplayName("Should handle AuthenticationException and return 401")
    void testHandleAuthenticationException() {
        AuthenticationException ex = new AuthenticationException("Invalid credentials");
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleApiException(ex, mockRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("AUTHENTICATION_ERROR", response.getBody().error().code());
    }

    @Test
    @DisplayName("Should handle ValidationException and return 400")
    void testHandleValidationException() {
        ValidationException ex = new ValidationException("Username cannot be empty");
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleApiException(ex, mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("VALIDATION_ERROR", response.getBody().error().code());
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException and return 404")
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleApiException(ex, mockRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().error().code());
    }

    @Test
    @DisplayName("Should handle generic Exception and return 500")
    void testHandleGenericException() {
        Exception ex = new Exception("Unexpected error");
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleGlobalException(ex, mockRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().error().code());
    }
}
