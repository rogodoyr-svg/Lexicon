package com.lexicon.prestamo.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PrestamoDto(
    UUID id,
    UUID libroId,
    String usuarioUsername,
    LocalDateTime fechaPrestamo,
    LocalDateTime fechaDevolucion,
    String estado,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
