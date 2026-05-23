package com.lexicon.libro.controller;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexicon.libro.dto.LibroDto;
import com.lexicon.libro.dto.LibroRequestDto;
import com.lexicon.libro.exception.GlobalExceptionHandler;
import com.lexicon.libro.exception.ResourceNotFoundException;
import com.lexicon.libro.exception.ValidationException;
import com.lexicon.libro.service.LibroService;

import java.time.LocalDateTime;

@WebMvcTest(controllers = LibroController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("LibroController Tests")
class LibroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private LibroService libroService;

    private UUID testId;
    private LibroDto testDto;
    private LibroRequestDto validRequest;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testDto = new LibroDto(testId, "Cien Años de Soledad", "Gabriel Garcia Marquez", "Novela", "978-3-16-148410-0", true, LocalDateTime.now(), LocalDateTime.now());
        validRequest = new LibroRequestDto("Cien Años de Soledad", "Gabriel Garcia Marquez", "Novela", "978-3-16-148410-0");
    }

    @Test
    @DisplayName("GET /api/libros - Should return all libros")
    void testGetAllLibros() throws Exception {
        when(libroService.getAllLibros()).thenReturn(List.of(testDto));

        mockMvc.perform(get("/api/libros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data[0].titulo", equalTo("Cien Años de Soledad")))
                .andExpect(jsonPath("$.data[0].autor", equalTo("Gabriel Garcia Marquez")));
    }

    @Test
    @DisplayName("GET /api/libros?autor=Garcia - Should search by autor")
    void testSearchByAutor() throws Exception {
        when(libroService.buscarLibros("Garcia", null)).thenReturn(List.of(testDto));

        mockMvc.perform(get("/api/libros").param("autor", "Garcia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data[0].titulo", equalTo("Cien Años de Soledad")));
    }

    @Test
    @DisplayName("GET /api/libros?genero=Novela - Should search by genero")
    void testSearchByGenero() throws Exception {
        when(libroService.buscarLibros(null, "Novela")).thenReturn(List.of(testDto));

        mockMvc.perform(get("/api/libros").param("genero", "Novela"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("GET /api/libros/{id} - Should return libro by ID")
    void testGetById() throws Exception {
        when(libroService.getLibroById(testId)).thenReturn(testDto);

        mockMvc.perform(get("/api/libros/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.titulo", equalTo("Cien Años de Soledad")));
    }

    @Test
    @DisplayName("GET /api/libros/{id} - Should return 404 when not found")
    void testGetByIdNotFound() throws Exception {
        when(libroService.getLibroById(testId)).thenThrow(new ResourceNotFoundException("Libro no encontrado"));

        mockMvc.perform(get("/api/libros/{id}", testId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", equalTo("RESOURCE_NOT_FOUND")));
    }

    @Test
    @DisplayName("POST /api/libros - Should create libro and return 201")
    void testCrearLibro() throws Exception {
        when(libroService.crearLibro(any(LibroRequestDto.class))).thenReturn(testDto);

        mockMvc.perform(post("/api/libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.titulo", equalTo("Cien Años de Soledad")));

        verify(libroService, times(1)).crearLibro(any(LibroRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/libros - Should return 400 for duplicate ISBN")
    void testCrearLibroDuplicateIsbn() throws Exception {
        when(libroService.crearLibro(any(LibroRequestDto.class)))
                .thenThrow(new ValidationException("Ya existe un libro con el ISBN: 978-3-16-148410-0"));

        mockMvc.perform(post("/api/libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", equalTo("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("PUT /api/libros/{id} - Should update libro")
    void testActualizarLibro() throws Exception {
        when(libroService.actualizarLibro(eq(testId), any(LibroRequestDto.class))).thenReturn(testDto);

        mockMvc.perform(put("/api/libros/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("DELETE /api/libros/{id} - Should delete libro")
    void testEliminarLibro() throws Exception {
        mockMvc.perform(delete("/api/libros/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        verify(libroService, times(1)).eliminarLibro(testId);
    }

    @Test
    @DisplayName("PATCH /api/libros/{id}/disponibilidad - Should update availability")
    void testActualizarDisponibilidad() throws Exception {
        mockMvc.perform(patch("/api/libros/{id}/disponibilidad", testId).param("disponible", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        verify(libroService, times(1)).actualizarDisponibilidad(testId, false);
    }

    @Test
    @DisplayName("GET /api/libros/{id}/disponibilidad - Should verify availability")
    void testVerificarDisponibilidad() throws Exception {
        when(libroService.verificarDisponibilidad(testId)).thenReturn(true);

        mockMvc.perform(get("/api/libros/{id}/disponibilidad", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(true)));
    }
}
