package com.lexicon.libro.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lexicon.libro.dto.LibroDto;
import com.lexicon.libro.dto.LibroRequestDto;
import com.lexicon.libro.entity.Libro;
import com.lexicon.libro.exception.ResourceNotFoundException;
import com.lexicon.libro.exception.ValidationException;
import com.lexicon.libro.repository.LibroRepository;

@Service
public class LibroService {

    private final LibroRepository libroRepository;

    public LibroService(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    public List<LibroDto> getAllLibros() {
        return libroRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public List<LibroDto> buscarLibros(String autor, String genero) {
        List<Libro> libros;

        if (autor != null && !autor.isBlank() && genero != null && !genero.isBlank()) {
            libros = libroRepository.findByAutorContainingIgnoreCaseAndGeneroContainingIgnoreCase(autor, genero);
        } else if (autor != null && !autor.isBlank()) {
            libros = libroRepository.findByAutorContainingIgnoreCase(autor);
        } else if (genero != null && !genero.isBlank()) {
            libros = libroRepository.findByGeneroContainingIgnoreCase(genero);
        } else {
            libros = libroRepository.findAll();
        }

        return libros.stream()
                .map(this::toDto)
                .toList();
    }

    public LibroDto getLibroById(UUID id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + id));
        return toDto(libro);
    }

    public LibroDto getLibroByIsbn(String isbn) {
        Libro libro = libroRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ISBN: " + isbn));
        return toDto(libro);
    }

    public LibroDto crearLibro(LibroRequestDto request) {
        if (libroRepository.findByIsbn(request.isbn()).isPresent()) {
            throw new ValidationException("Ya existe un libro con el ISBN: " + request.isbn());
        }

        Libro libro = Libro.builder()
                .titulo(request.titulo())
                .autor(request.autor())
                .genero(request.genero())
                .isbn(request.isbn())
                .disponible(true)
                .build();

        libroRepository.save(libro);
        return toDto(libro);
    }

    public LibroDto actualizarLibro(UUID id, LibroRequestDto request) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + id));

        libro.setTitulo(request.titulo());
        libro.setAutor(request.autor());
        libro.setGenero(request.genero());
        libro.setIsbn(request.isbn());

        libroRepository.save(libro);
        return toDto(libro);
    }

    public void eliminarLibro(UUID id) {
        if (!libroRepository.existsById(id)) {
            throw new ResourceNotFoundException("Libro no encontrado con ID: " + id);
        }
        libroRepository.deleteById(id);
    }

    public void actualizarDisponibilidad(UUID id, boolean disponible) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + id));

        libro.setDisponible(disponible);
        libroRepository.save(libro);
    }

    public boolean verificarDisponibilidad(UUID id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + id));
        return libro.getDisponible();
    }

    public List<LibroDto> getLibrosDisponibles() {
        return libroRepository.findByDisponibleTrue().stream()
                .map(this::toDto)
                .toList();
    }

    private LibroDto toDto(Libro libro) {
        return new LibroDto(
                libro.getId(),
                libro.getTitulo(),
                libro.getAutor(),
                libro.getGenero(),
                libro.getIsbn(),
                libro.getDisponible(),
                libro.getCreatedAt(),
                libro.getUpdatedAt()
        );
    }
}
