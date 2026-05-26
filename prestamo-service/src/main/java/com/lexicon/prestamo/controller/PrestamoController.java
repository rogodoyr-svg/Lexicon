package com.lexicon.prestamo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lexicon.prestamo.dto.ApiResponse;
import com.lexicon.prestamo.dto.PrestamoDto;
import com.lexicon.prestamo.dto.PrestamoRequestDto;
import com.lexicon.prestamo.service.PrestamoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PrestamoDto>>> getAllPrestamos() {
        List<PrestamoDto> prestamos = prestamoService.getAllPrestamos();
        ApiResponse<List<PrestamoDto>> response = new ApiResponse<>(true, prestamos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuario")
    public ResponseEntity<ApiResponse<List<PrestamoDto>>> getPrestamosByUsuario(
            Authentication authentication) {
        String username = authentication.getName();
        List<PrestamoDto> prestamos = prestamoService.getPrestamosByUsuario(username);
        ApiResponse<List<PrestamoDto>> response = new ApiResponse<>(true, prestamos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estado")
    public ResponseEntity<ApiResponse<List<PrestamoDto>>> getPrestamosByEstado(
            @RequestParam String estado) {
        List<PrestamoDto> prestamos = prestamoService.getPrestamosByEstado(estado);
        ApiResponse<List<PrestamoDto>> response = new ApiResponse<>(true, prestamos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrestamoDto>> getPrestamoById(@PathVariable UUID id) {
        PrestamoDto prestamo = prestamoService.getPrestamoById(id);
        ApiResponse<PrestamoDto> response = new ApiResponse<>(true, prestamo);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PrestamoDto>> registrarPrestamo(
            Authentication authentication,
            @Valid @RequestBody PrestamoRequestDto request) {
        String username = authentication.getName();
        PrestamoDto prestamo = prestamoService.registrarPrestamo(username, request);
        ApiResponse<PrestamoDto> response = new ApiResponse<>(true, prestamo);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/devolucion")
    public ResponseEntity<ApiResponse<PrestamoDto>> devolverLibro(@PathVariable UUID id) {
        PrestamoDto prestamo = prestamoService.devolverLibro(id);
        ApiResponse<PrestamoDto> response = new ApiResponse<>(true, prestamo);
        return ResponseEntity.ok(response);
    }

    // mostrar todos los usuarios
    @GetMapping("/usuarios")
    public ResponseEntity<ApiResponse<List<String>>> getAllUsuarios() {
        List<String> usuarios = prestamoService.getAllUsuarios();
        ApiResponse<List<String>> response = new ApiResponse<>(true, usuarios);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> eliminarPrestamo(@PathVariable UUID id) {
        prestamoService.eliminarPrestamo(id);
        ApiResponse<String> response = new ApiResponse<>(true, "Prestamo eliminado con exito");
        return ResponseEntity.ok(response);
    }
}
