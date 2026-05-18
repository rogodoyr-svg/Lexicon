package com.lexicon.prestamo.dto;

public record AuthResponse(
    String token,
    long expiresIn,
    String username
) {
}
