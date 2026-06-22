package com.lexicon.prestamo.client;

import java.util.UUID;

import com.lexicon.prestamo.dto.LibroDto;

public interface LibroServiceClient {

    LibroDto getLibroById(UUID id);

    void actualizarDisponibilidad(UUID id, boolean disponible);

    
}
