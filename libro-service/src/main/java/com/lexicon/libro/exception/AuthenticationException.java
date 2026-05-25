package com.lexicon.libro.exception;
import org.springframework.http.HttpStatus;

public class AuthenticationException {
     public AuthenticationException(String message) {
        super("AUTHENTICATION_ERROR", message, HttpStatus.UNAUTHORIZED);
    }
}
