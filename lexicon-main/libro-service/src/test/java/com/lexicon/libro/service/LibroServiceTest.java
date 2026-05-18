package com.lexicon.libro.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import com.lexicon.libro.dto.LibroDto;
import com.lexicon.libro.dto.LibroRequestDto;
import com.lexicon.libro.entity.Libro;
import com.lexicon.libro.exception.ResourceNotFoundException;
import com.lexicon.libro.exception.ValidationException;
import com.lexicon.libro.repository.LibroRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("LibroService Tests")
class LibroServiceTest {

    @Mock
    private LibroRepository libroRepository;

    @InjectMocks
    private LibroService libroService;

    private Libro testLibro;
    private LibroRequestDto validRequest;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testLibro = Libro.builder()
                .id(testId)
                .titulo("Cien Años de Soledad")
                .autor("Gabriel Garcia Marquez")
                .genero("Novela")
                .isbn("978-3-16-148410-0")
                .disponible(true)
                .build();

        validRequest = new LibroRequestDto("Cien Años de Soledad", "Gabriel Garcia Marquez", "Novela", "978-3-16-148410-0");
    }

    @Test
    @DisplayName("Should get all libros")
    void testGetAllLibros() {
        when(libroRepository.findAll()).thenReturn(List.of(testLibro));
        List<LibroDto> result = libroService.getAllLibros();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cien Años de Soledad", result.get(0).titulo());
    }

    @Test
    @DisplayName("Should get libro by ID")
    void testGetLibroById() {
        when(libroRepository.findById(testId)).thenReturn(Optional.of(testLibro));
        LibroDto result = libroService.getLibroById(testId);

        assertNotNull(result);
        assertEquals(testId, result.id());
        assertEquals("Cien Años de Soledad", result.titulo());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when libro not found by ID")
    void testGetLibroByIdNotFound() {
        when(libroRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> libroService.getLibroById(testId));
    }

    @Test
    @DisplayName("Should get libro by ISBN")
    void testGetLibroByIsbn() {
        when(libroRepository.findByIsbn("978-3-16-148410-0")).thenReturn(Optional.of(testLibro));
        LibroDto result = libroService.getLibroByIsbn("978-3-16-148410-0");

        assertNotNull(result);
        assertEquals("978-3-16-148410-0", result.isbn());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when libro not found by ISBN")
    void testGetLibroByIsbnNotFound() {
        when(libroRepository.findByIsbn("000-0-00-000000-0")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> libroService.getLibroByIsbn("000-0-00-000000-0"));
    }

    @Test
    @DisplayName("Should create a new libro")
    void testCrearLibro() {
        when(libroRepository.findByIsbn("978-3-16-148410-0")).thenReturn(Optional.empty());
        when(libroRepository.save(any(Libro.class))).thenReturn(testLibro);

        LibroDto result = libroService.crearLibro(validRequest);

        assertNotNull(result);
        assertEquals("Cien Años de Soledad", result.titulo());
        verify(libroRepository, times(1)).save(any(Libro.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when creating libro with duplicate ISBN")
    void testCrearLibroDuplicateIsbn() {
        when(libroRepository.findByIsbn("978-3-16-148410-0")).thenReturn(Optional.of(testLibro));

        assertThrows(ValidationException.class, () -> libroService.crearLibro(validRequest));
        verify(libroRepository, never()).save(any(Libro.class));
    }

    @Test
    @DisplayName("Should update an existing libro")
    void testActualizarLibro() {
        LibroRequestDto updateRequest = new LibroRequestDto("Nuevo Titulo", "Nuevo Autor", "Ciencia Ficcion", "978-3-16-148410-0");
        when(libroRepository.findById(testId)).thenReturn(Optional.of(testLibro));
        when(libroRepository.save(any(Libro.class))).thenReturn(testLibro);

        LibroDto result = libroService.actualizarLibro(testId, updateRequest);

        assertNotNull(result);
        verify(libroRepository, times(1)).save(any(Libro.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent libro")
    void testActualizarLibroNotFound() {
        when(libroRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> libroService.actualizarLibro(testId, validRequest));
    }

    @Test
    @DisplayName("Should delete an existing libro")
    void testEliminarLibro() {
        when(libroRepository.existsById(testId)).thenReturn(true);

        assertDoesNotThrow(() -> libroService.eliminarLibro(testId));
        verify(libroRepository, times(1)).deleteById(testId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent libro")
    void testEliminarLibroNotFound() {
        when(libroRepository.existsById(testId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> libroService.eliminarLibro(testId));
    }

    @Test
    @DisplayName("Should update availability")
    void testActualizarDisponibilidad() {
        when(libroRepository.findById(testId)).thenReturn(Optional.of(testLibro));

        assertDoesNotThrow(() -> libroService.actualizarDisponibilidad(testId, false));
        verify(libroRepository, times(1)).save(any(Libro.class));
    }

    @Test
    @DisplayName("Should verify availability - true")
    void testVerificarDisponibilidadTrue() {
        when(libroRepository.findById(testId)).thenReturn(Optional.of(testLibro));

        assertTrue(libroService.verificarDisponibilidad(testId));
    }

    @Test
    @DisplayName("Should verify availability - false")
    void testVerificarDisponibilidadFalse() {
        testLibro.setDisponible(false);
        when(libroRepository.findById(testId)).thenReturn(Optional.of(testLibro));

        assertFalse(libroService.verificarDisponibilidad(testId));
    }

    @Test
    @DisplayName("Should search libros by autor")
    void testBuscarLibrosByAutor() {
        when(libroRepository.findByAutorContainingIgnoreCase("Garcia")).thenReturn(List.of(testLibro));

        List<LibroDto> result = libroService.buscarLibros("Garcia", null);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should search libros by genero")
    void testBuscarLibrosByGenero() {
        when(libroRepository.findByGeneroContainingIgnoreCase("Novela")).thenReturn(List.of(testLibro));

        List<LibroDto> result = libroService.buscarLibros(null, "Novela");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should search libros by autor and genero")
    void testBuscarLibrosByAutorAndGenero() {
        when(libroRepository.findByAutorContainingIgnoreCaseAndGeneroContainingIgnoreCase("Garcia", "Novela"))
                .thenReturn(List.of(testLibro));

        List<LibroDto> result = libroService.buscarLibros("Garcia", "Novela");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should get available libros")
    void testGetLibrosDisponibles() {
        when(libroRepository.findByDisponibleTrue()).thenReturn(List.of(testLibro));
        List<LibroDto> result = libroService.getLibrosDisponibles();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).disponible());
    }
}
