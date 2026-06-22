package com.lexicon.libro.dto;

import jakarta.validation.constraints.NotBlank;

public record LibroRequestDto(
    
    @NotBlank(message = "El titulo es obligatorio")
    String titulo,

    @NotBlank(message = "El autor es obligatorio")
    String autor,

    @NotBlank(message = "El genero es obligatorio")
    String genero,

    @NotBlank(message = "El ISBN es obligatorio")
    String isbn
) {
}
