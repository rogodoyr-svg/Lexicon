package com.lexicon.libro.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lexicon.libro.entity.Libro;

@Repository
public interface LibroRepository extends JpaRepository<Libro, UUID> {

    Optional<Libro> findByIsbn(String isbn);

    List<Libro> findByAutorContainingIgnoreCase(String autor);

    List<Libro> findByGeneroContainingIgnoreCase(String genero);

    List<Libro> findByAutorContainingIgnoreCaseAndGeneroContainingIgnoreCase(String autor, String genero);

    List<Libro> findByDisponibleTrue();
}
