package com.lexicon.prestamo.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lexicon.prestamo.entity.Prestamo;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, UUID> {

    List<Prestamo> findByUsuarioUsername(String usuarioUsername);

    List<Prestamo> findByEstado(String estado);

    Optional<Prestamo> findByLibroIdAndEstado(UUID libroId, String estado);
}
