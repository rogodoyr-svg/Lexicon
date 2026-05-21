package com.lexicon.prestamo.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record PrestamoRequestDto(
    @NotNull(message = "El ID del libro es obligatorio")
    UUID libroId
) {
}
