package com.lexicon.libro.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lexicon.libro.dto.ApiResponse;
import com.lexicon.libro.dto.LibroDto;
import com.lexicon.libro.dto.LibroRequestDto;
import com.lexicon.libro.service.LibroService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LibroDto>>> getAllLibros(
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) String genero) {

        List<LibroDto> libros;

        boolean hasFilters = (autor != null && !autor.isBlank()) || (genero != null && !genero.isBlank());

        if (hasFilters) {
            libros = libroService.buscarLibros(autor, genero);
        } else {
            libros = libroService.getAllLibros();
        }

        ApiResponse<List<LibroDto>> response = new ApiResponse<>(true, libros);
        return ResponseEntity.ok(response);
    }


    //obtener los libros que estan disponibles
    @GetMapping("/disponibles")
    public ResponseEntity<ApiResponse<List<LibroDto>>> getLibrosDisponibles() {
        List<LibroDto> libros = libroService.getLibrosDisponibles();
        ApiResponse<List<LibroDto>> response = new ApiResponse<>(true, libros);
        return ResponseEntity.ok(response);
    }

    //obtener libro por id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LibroDto>> getLibroById(@PathVariable UUID id) {
        LibroDto libro = libroService.getLibroById(id);
        ApiResponse<LibroDto> response = new ApiResponse<>(true, libro);
        return ResponseEntity.ok(response);
    }

    //obtener libro por codigo de identificacion unico para libros
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<ApiResponse<LibroDto>> getLibroByIsbn(@PathVariable String isbn) {
        LibroDto libro = libroService.getLibroByIsbn(isbn);
        ApiResponse<LibroDto> response = new ApiResponse<>(true, libro);
        return ResponseEntity.ok(response);
    }

    //Funcion para crear libro
    @PostMapping
    public ResponseEntity<ApiResponse<LibroDto>> crearLibro(
            @Valid @RequestBody LibroRequestDto request) {
        LibroDto libro = libroService.crearLibro(request);
        ApiResponse<LibroDto> response = new ApiResponse<>(true, libro);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //funcion para actualizar libros
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LibroDto>> actualizarLibro(
            @PathVariable UUID id,
            @Valid @RequestBody LibroRequestDto request) {
        LibroDto libro = libroService.actualizarLibro(id, request);
        ApiResponse<LibroDto> response = new ApiResponse<>(true, libro);
        return ResponseEntity.ok(response);
    }

    //funcion para eliminar libro
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarLibro(@PathVariable UUID id) {
        libroService.eliminarLibro(id);
        ApiResponse<Void> response = new ApiResponse<>(true, null);
        return ResponseEntity.ok(response);
    }


    //funcion para actualizar la disponiblidad por id
    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<ApiResponse<Void>> actualizarDisponibilidad(
            @PathVariable UUID id,
            @RequestParam boolean disponible) {
        libroService.actualizarDisponibilidad(id, disponible);
        ApiResponse<Void> response = new ApiResponse<>(true, null);
        return ResponseEntity.ok(response);
    }

    //funcion para verificar la disponibilidad por id
    @GetMapping("/{id}/disponibilidad")
    public ResponseEntity<ApiResponse<Boolean>> verificarDisponibilidad(@PathVariable UUID id) {
        boolean disponible = libroService.verificarDisponibilidad(id);
        ApiResponse<Boolean> response = new ApiResponse<>(true, disponible);
        return ResponseEntity.ok(response);
    }
}
