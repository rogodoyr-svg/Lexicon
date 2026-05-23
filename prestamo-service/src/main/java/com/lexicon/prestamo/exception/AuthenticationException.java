package com.lexicon.prestamo.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends ApiException {
    public AuthenticationException(String message) {
        super("AUTHENTICATION_ERROR", message, HttpStatus.UNAUTHORIZED);
    }
}
