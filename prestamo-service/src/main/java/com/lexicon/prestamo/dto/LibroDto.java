package com.lexicon.prestamo.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LibroDto(
    UUID id,
    String titulo,
    String autor,
    String genero,
    String isbn,
    Boolean disponible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
