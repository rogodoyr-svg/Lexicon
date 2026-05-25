package com.lexicon.libro.exception;
import org.springframework.http.HttpStatus;

public class AuthorizationException {
    public AuthorizationException(String message) {
        super("AUTHORIZATION_ERROR", message, HttpStatus.FORBIDDEN);
    }
}
