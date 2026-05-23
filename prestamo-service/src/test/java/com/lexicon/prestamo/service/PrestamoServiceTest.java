package com.lexicon.prestamo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lexicon.prestamo.client.LibroServiceClient;
import com.lexicon.prestamo.dto.LibroDto;
import com.lexicon.prestamo.dto.PrestamoDto;
import com.lexicon.prestamo.dto.PrestamoRequestDto;
import com.lexicon.prestamo.entity.Prestamo;
import com.lexicon.prestamo.exception.ResourceNotFoundException;
import com.lexicon.prestamo.exception.ValidationException;
import com.lexicon.prestamo.repository.PrestamoRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("PrestamoService Tests")
class PrestamoServiceTest {

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private LibroServiceClient libroServiceClient;

    @InjectMocks
    private PrestamoService prestamoService;

    private UUID testLibroId;
    private UUID testPrestamoId;
    private LibroDto availableBook;
    private LibroDto unavailableBook;
    private Prestamo testPrestamo;
    private PrestamoRequestDto validRequest;

    @BeforeEach
    void setUp() {
        testLibroId = UUID.randomUUID();
        testPrestamoId = UUID.randomUUID();
        availableBook = new LibroDto(testLibroId, "Cien Años de Soledad", "Gabriel Garcia Marquez", "Novela", "978-3-16-148410-0", true, LocalDateTime.now(), LocalDateTime.now());
        unavailableBook = new LibroDto(testLibroId, "Cien Años de Soledad", "Gabriel Garcia Marquez", "Novela", "978-3-16-148410-0", false, LocalDateTime.now(), LocalDateTime.now());
        testPrestamo = Prestamo.builder()
                .id(testPrestamoId)
                .libroId(testLibroId)
                .usuarioUsername("claudio")
                .fechaPrestamo(LocalDateTime.now())
                .estado("ACTIVO")
                .build();
        validRequest = new PrestamoRequestDto(testLibroId);
    }

    @Test
    @DisplayName("Should get all prestamos")
    void testGetAllPrestamos() {
        when(prestamoRepository.findAll()).thenReturn(List.of(testPrestamo));
        List<PrestamoDto> result = prestamoService.getAllPrestamos();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACTIVO", result.get(0).estado());
    }

    @Test
    @DisplayName("Should get prestamos by usuario")
    void testGetPrestamosByUsuario() {
        when(prestamoRepository.findByUsuarioUsername("claudio")).thenReturn(List.of(testPrestamo));
        List<PrestamoDto> result = prestamoService.getPrestamosByUsuario("claudio");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should get prestamos by estado")
    void testGetPrestamosByEstado() {
        when(prestamoRepository.findByEstado("ACTIVO")).thenReturn(List.of(testPrestamo));
        List<PrestamoDto> result = prestamoService.getPrestamosByEstado("ACTIVO");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should get prestamo by ID")
    void testGetPrestamoById() {
        when(prestamoRepository.findById(testPrestamoId)).thenReturn(Optional.of(testPrestamo));
        PrestamoDto result = prestamoService.getPrestamoById(testPrestamoId);

        assertNotNull(result);
        assertEquals(testPrestamoId, result.id());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when prestamo not found")
    void testGetPrestamoByIdNotFound() {
        when(prestamoRepository.findById(testPrestamoId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> prestamoService.getPrestamoById(testPrestamoId));
    }

    @Test
    @DisplayName("Should register prestamo successfully")
    void testRegistrarPrestamoSuccess() {
        when(libroServiceClient.getLibroById(testLibroId)).thenReturn(availableBook);
        when(prestamoRepository.findByLibroIdAndEstado(testLibroId, "ACTIVO")).thenReturn(Optional.empty());
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> {
            Prestamo p = inv.getArgument(0);
            p.setId(testPrestamoId);
            return p;
        });

        PrestamoDto result = prestamoService.registrarPrestamo("claudio", validRequest);

        assertNotNull(result);
        assertEquals("ACTIVO", result.estado());
        assertEquals("claudio", result.usuarioUsername());
        verify(libroServiceClient, times(1)).actualizarDisponibilidad(testLibroId, false);
        verify(prestamoRepository, times(1)).save(any(Prestamo.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when book not found")
    void testRegistrarPrestamoBookNotFound() {
        when(libroServiceClient.getLibroById(testLibroId)).thenThrow(new RuntimeException("Not found"));

        assertThrows(ResourceNotFoundException.class, () -> prestamoService.registrarPrestamo("claudio", validRequest));
    }

    @Test
    @DisplayName("Should throw ValidationException when book not available")
    void testRegistrarPrestamoBookNotAvailable() {
        when(libroServiceClient.getLibroById(testLibroId)).thenReturn(unavailableBook);

        assertThrows(ValidationException.class, () -> prestamoService.registrarPrestamo("claudio", validRequest));
        verify(libroServiceClient, never()).actualizarDisponibilidad(any(), anyBoolean());
    }

    @Test
    @DisplayName("Should throw ValidationException when active loan already exists for book")
    void testRegistrarPrestamoActiveLoanExists() {
        when(libroServiceClient.getLibroById(testLibroId)).thenReturn(availableBook);
        when(prestamoRepository.findByLibroIdAndEstado(testLibroId, "ACTIVO")).thenReturn(Optional.of(testPrestamo));

        assertThrows(ValidationException.class, () -> prestamoService.registrarPrestamo("claudio", validRequest));
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return book successfully")
    void testDevolverLibroSuccess() {
        when(prestamoRepository.findById(testPrestamoId)).thenReturn(Optional.of(testPrestamo));
        when(prestamoRepository.save(any(Prestamo.class))).thenReturn(testPrestamo);

        PrestamoDto result = prestamoService.devolverLibro(testPrestamoId);

        assertNotNull(result);
        assertEquals("DEVUELTO", result.estado());
        assertNotNull(result.fechaDevolucion());
        verify(libroServiceClient, times(1)).actualizarDisponibilidad(testLibroId, true);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when returning non-existent prestamo")
    void testDevolverLibroNotFound() {
        when(prestamoRepository.findById(testPrestamoId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> prestamoService.devolverLibro(testPrestamoId));
    }

    @Test
    @DisplayName("Should throw ValidationException when returning already returned book")
    void testDevolverLibroAlreadyReturned() {
        testPrestamo.setEstado("DEVUELTO");
        testPrestamo.setFechaDevolucion(LocalDateTime.now());
        when(prestamoRepository.findById(testPrestamoId)).thenReturn(Optional.of(testPrestamo));

        assertThrows(ValidationException.class, () -> prestamoService.devolverLibro(testPrestamoId));
        verify(libroServiceClient, never()).actualizarDisponibilidad(any(), anyBoolean());
    }
}
