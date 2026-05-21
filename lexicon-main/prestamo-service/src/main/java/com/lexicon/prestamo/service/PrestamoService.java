package com.lexicon.prestamo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lexicon.prestamo.client.LibroServiceClient;
import com.lexicon.prestamo.dto.LibroDto;
import com.lexicon.prestamo.dto.PrestamoDto;
import com.lexicon.prestamo.dto.PrestamoRequestDto;
import com.lexicon.prestamo.entity.Prestamo;
import com.lexicon.prestamo.exception.ResourceNotFoundException;
import com.lexicon.prestamo.exception.ValidationException;
import com.lexicon.prestamo.repository.PrestamoRepository;

@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final LibroServiceClient libroServiceClient;

    public PrestamoService(PrestamoRepository prestamoRepository, LibroServiceClient libroServiceClient) {
        this.prestamoRepository = prestamoRepository;
        this.libroServiceClient = libroServiceClient;
    }

    public List<PrestamoDto> getAllPrestamos() {
        return prestamoRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public List<PrestamoDto> getPrestamosByUsuario(String username) {
        return prestamoRepository.findByUsuarioUsername(username).stream()
                .map(this::toDto)
                .toList();
    }

    public List<PrestamoDto> getPrestamosByEstado(String estado) {
        return prestamoRepository.findByEstado(estado).stream()
                .map(this::toDto)
                .toList();
    }

    public PrestamoDto getPrestamoById(UUID id) {
        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo no encontrado con ID: " + id));
        return toDto(prestamo);
    }

    @Transactional
    public PrestamoDto registrarPrestamo(String username, PrestamoRequestDto request) {
        // Verify book exists and is available via Libro-Service
        LibroDto libro;
        try {
            libro = libroServiceClient.getLibroById(request.libroId());
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Libro no encontrado con ID: " + request.libroId());
        }

        if (!libro.disponible()) {
            throw new ValidationException("El libro '" + libro.titulo() + "' no se encuentra disponible para prestamo");
        }

        // Check if user already has an active loan for this book
        prestamoRepository.findByLibroIdAndEstado(request.libroId(), "ACTIVO")
                .ifPresent(p -> {
                    throw new ValidationException("Ya existe un prestamo activo para este libro");
                });

        // Register the loan
        Prestamo prestamo = Prestamo.builder()
                .libroId(request.libroId())
                .usuarioUsername(username)
                .fechaPrestamo(LocalDateTime.now())
                .estado("ACTIVO")
                .build();

        prestamoRepository.save(prestamo);

        // Update book availability in Libro-Service
        libroServiceClient.actualizarDisponibilidad(request.libroId(), false);

        return toDto(prestamo);
    }

    @Transactional
    public PrestamoDto devolverLibro(UUID prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo no encontrado con ID: " + prestamoId));

        if ("DEVUELTO".equals(prestamo.getEstado())) {
            throw new ValidationException("Este prestamo ya fue devuelto");
        }

        prestamo.setEstado("DEVUELTO");
        prestamo.setFechaDevolucion(LocalDateTime.now());
        prestamoRepository.save(prestamo);

        // Update book availability in Libro-Service
        libroServiceClient.actualizarDisponibilidad(prestamo.getLibroId(), true);

        return toDto(prestamo);
    }

    private PrestamoDto toDto(Prestamo prestamo) {
        return new PrestamoDto(
                prestamo.getId(),
                prestamo.getLibroId(),
                prestamo.getUsuarioUsername(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaDevolucion(),
                prestamo.getEstado(),
                prestamo.getCreatedAt(),
                prestamo.getUpdatedAt()
        );
    }
}
