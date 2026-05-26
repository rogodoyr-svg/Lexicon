package com.lexicon.prestamo.dto;

public record LoginRequest(
    String username,
    String password
) {
}
