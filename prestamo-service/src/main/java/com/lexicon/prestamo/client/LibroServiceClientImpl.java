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
        org.springframework.core.ParameterizedTypeReference<com.lexicon.prestamo.dto.ApiResponse<LibroDto>> typeRef = 
                new org.springframework.core.ParameterizedTypeReference<>() {};

        ResponseEntity<com.lexicon.prestamo.dto.ApiResponse<LibroDto>> response = restClient.get()
                .uri("/libros/{id}", id)
                .retrieve()
                .toEntity(typeRef);

        com.lexicon.prestamo.dto.ApiResponse<LibroDto> body = response.getBody();
        HttpStatusCode status = response.getStatusCode();
        if (status.is2xxSuccessful() && body != null && body.success() && body.data() != null) {
            return body.data();
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
