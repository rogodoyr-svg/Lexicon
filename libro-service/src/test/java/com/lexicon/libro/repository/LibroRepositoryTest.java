package com.lexicon.libro.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import com.lexicon.libro.entity.Libro;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("LibroRepository Tests")
class LibroRepositoryTest {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Libro testLibro;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testLibro = Libro.builder()
                .titulo("Cien Años de Soledad")
                .autor("Gabriel Garcia Marquez")
                .genero("Novela")
                .isbn("978-3-16-148410-0")
                .disponible(true)
                .build();
    }

    @Test
    @DisplayName("Should save libro and retrieve by id")
    void testSaveAndRetrieveById() {
        Libro saved = libroRepository.save(testLibro);
        Optional<Libro> found = libroRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Cien Años de Soledad", found.get().getTitulo());
    }

    @Test
    @DisplayName("Should find by ISBN")
    void testFindByIsbn() {
        entityManager.persistAndFlush(testLibro);
        Optional<Libro> found = libroRepository.findByIsbn("978-3-16-148410-0");

        assertTrue(found.isPresent());
        assertEquals("Gabriel Garcia Marquez", found.get().getAutor());
    }

    @Test
    @DisplayName("Should return empty when ISBN not found")
    void testFindByIsbnNotFound() {
        Optional<Libro> found = libroRepository.findByIsbn("000-0-00-000000-0");
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should find by autor containing ignore case")
    void testFindByAutorContainingIgnoreCase() {
        entityManager.persistAndFlush(testLibro);
        List<Libro> results = libroRepository.findByAutorContainingIgnoreCase("garcia");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Should find by genero containing ignore case")
    void testFindByGeneroContainingIgnoreCase() {
        entityManager.persistAndFlush(testLibro);
        List<Libro> results = libroRepository.findByGeneroContainingIgnoreCase("novela");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Should find by autor and genero")
    void testFindByAutorAndGenero() {
        entityManager.persistAndFlush(testLibro);
        List<Libro> results = libroRepository.findByAutorContainingIgnoreCaseAndGeneroContainingIgnoreCase("garcia", "novela");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Should find available libros")
    void testFindByDisponibleTrue() {
        entityManager.persistAndFlush(testLibro);
        List<Libro> results = libroRepository.findByDisponibleTrue();

        assertFalse(results.isEmpty());
        results.forEach(l -> assertTrue(l.getDisponible()));
    }

    @Test
    @DisplayName("Should assign UUID on save")
    void testUUIDAssignedOnSave() {
        assertNull(testLibro.getId());
        Libro saved = libroRepository.save(testLibro);

        assertNotNull(saved.getId());
        assertTrue(saved.getId() instanceof UUID);
    }

    @Test
    @DisplayName("Should persist timestamps")
    void testTimestampsPersisted() {
        Libro saved = libroRepository.save(testLibro);

        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    @DisplayName("Should enforce ISBN uniqueness")
    void testIsbnUniqueConstraint() {
        Libro libro1 = Libro.builder().isbn("999-9-99-999999-9").titulo("A").autor("B").genero("C").build();
        Libro libro2 = Libro.builder().isbn("999-9-99-999999-9").titulo("X").autor("Y").genero("Z").build();

        libroRepository.save(libro1);
        entityManager.flush();

        assertThrows(Exception.class, () -> {
            libroRepository.save(libro2);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should delete libro by id")
    void testDeleteById() {
        Libro saved = libroRepository.save(testLibro);
        UUID id = saved.getId();

        libroRepository.deleteById(id);
        assertTrue(libroRepository.findById(id).isEmpty());
    }

    @Test
    @DisplayName("Should count libros correctly")
    void testCount() {
        libroRepository.save(testLibro);
        libroRepository.save(Libro.builder().titulo("B").autor("A").genero("G").isbn("111-1-11-111111-1").build());

        assertEquals(2, libroRepository.count());
    }
}
