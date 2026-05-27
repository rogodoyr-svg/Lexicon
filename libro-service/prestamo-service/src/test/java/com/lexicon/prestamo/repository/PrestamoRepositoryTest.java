package com.lexicon.prestamo.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import com.lexicon.prestamo.entity.Prestamo;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("PrestamoRepository Tests")
class PrestamoRepositoryTest {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Prestamo testPrestamo;
    private UUID testLibroId;

    @BeforeEach
    void setUp() {
        testLibroId = UUID.randomUUID();
        testPrestamo = Prestamo.builder()
                .libroId(testLibroId)
                .usuarioUsername("claudio")
                .estado("ACTIVO")
                .build();
    }

    @Test
    @DisplayName("Should save and retrieve by id")
    void testSaveAndRetrieveById() {
        Prestamo saved = prestamoRepository.save(testPrestamo);
        Optional<Prestamo> found = prestamoRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("ACTIVO", found.get().getEstado());
    }

    @Test
    @DisplayName("Should find by usuario username")
    void testFindByUsuarioUsername() {
        entityManager.persistAndFlush(testPrestamo);
        List<Prestamo> results = prestamoRepository.findByUsuarioUsername("claudio");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Should find by estado")
    void testFindByEstado() {
        entityManager.persistAndFlush(testPrestamo);
        List<Prestamo> results = prestamoRepository.findByEstado("ACTIVO");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Should find by libroId and estado")
    void testFindByLibroIdAndEstado() {
        entityManager.persistAndFlush(testPrestamo);
        Optional<Prestamo> found = prestamoRepository.findByLibroIdAndEstado(testLibroId, "ACTIVO");

        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("Should assign UUID on save")
    void testUUIDAssignedOnSave() {
        assertNull(testPrestamo.getId());
        Prestamo saved = prestamoRepository.save(testPrestamo);
        assertNotNull(saved.getId());
        assertTrue(saved.getId() instanceof UUID);
    }

    @Test
    @DisplayName("Should delete by id")
    void testDeleteById() {
        Prestamo saved = prestamoRepository.save(testPrestamo);
        prestamoRepository.deleteById(saved.getId());
        assertTrue(prestamoRepository.findById(saved.getId()).isEmpty());
    }
}
