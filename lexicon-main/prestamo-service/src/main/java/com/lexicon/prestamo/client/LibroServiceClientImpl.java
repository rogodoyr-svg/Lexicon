package com.lexicon.prestamo.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.lexicon.prestamo.dto.LibroDto;

@Component
public class LibroServiceClientImpl implements LibroServiceClient {

    private final RestClient restClient;

    public LibroServiceClientImpl(@Value("${apiLibro.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public LibroDto getLibroById(UUID id) {
        ResponseEntity<LibroDto> response = restClient.get()
                .uri("/libros/{id}", id)
                .retrieve()
                .toEntity(LibroDto.class);

        LibroDto body = response.getBody();
        HttpStatusCode status = response.getStatusCode();
        if (status.is2xxSuccessful() && body != null) {
            return body;
        } else {
            throw new RuntimeException("Failed to fetch libro: " + status);
        }
    }

    @Override
    public void actualizarDisponibilidad(UUID id, boolean disponible) {
        ResponseEntity<Void> response = restClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/libros/{id}/disponibilidad")
                        .queryParam("disponible", disponible)
                        .build(id))
                .retrieve()
                .toBodilessEntity();

        HttpStatusCode status = response.getStatusCode();
        if (!status.is2xxSuccessful()) {
            throw new RuntimeException("Failed to update libro availability: " + status);
        }
    }
}
