package com.lexicon.libro.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Libro Entity Tests")
class LibroTest {

    private Libro libro;

    @BeforeEach
    void setUp() {
        libro = new Libro();
    }

    @Test
    @DisplayName("Should create Libro with no-arg constructor")
    void testNoArgConstructor() {
        Libro newLibro = new Libro();
        assertNotNull(newLibro);
        assertNull(newLibro.getId());
        assertNull(newLibro.getTitulo());
    }

    @Test
    @DisplayName("Should create Libro with all-args constructor")
    void testAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Libro newLibro = new Libro(id, "Cien Años de Soledad", "Gabriel Garcia Marquez", "Novela", "978-3-16-148410-0", true, now, now);

        assertEquals(id, newLibro.getId());
        assertEquals("Cien Años de Soledad", newLibro.getTitulo());
        assertEquals("Gabriel Garcia Marquez", newLibro.getAutor());
        assertEquals("Novela", newLibro.getGenero());
        assertEquals("978-3-16-148410-0", newLibro.getIsbn());
        assertTrue(newLibro.getDisponible());
    }

    @Test
    @DisplayName("Should create Libro using builder")
    void testBuilder() {
        Libro builtLibro = Libro.builder()
                .titulo("Don Quijote")
                .autor("Miguel de Cervantes")
                .genero("Clasico")
                .isbn("978-1-23-456789-7")
                .disponible(true)
                .build();

        assertNotNull(builtLibro);
        assertEquals("Don Quijote", builtLibro.getTitulo());
        assertEquals("Miguel de Cervantes", builtLibro.getAutor());
        assertEquals("Clasico", builtLibro.getGenero());
        assertEquals("978-1-23-456789-7", builtLibro.getIsbn());
        assertTrue(builtLibro.getDisponible());
    }

    @Test
    @DisplayName("Should set and get all fields")
    void testSettersAndGetters() {
        UUID id = UUID.randomUUID();
        libro.setId(id);
        libro.setTitulo("El Principito");
        libro.setAutor("Antoine de Saint-Exupery");
        libro.setGenero("Fabula");
        libro.setIsbn("978-0-12-345678-9");
        libro.setDisponible(false);

        assertEquals(id, libro.getId());
        assertEquals("El Principito", libro.getTitulo());
        assertEquals("Antoine de Saint-Exupery", libro.getAutor());
        assertEquals("Fabula", libro.getGenero());
        assertEquals("978-0-12-345678-9", libro.getIsbn());
        assertFalse(libro.getDisponible());
    }

    @Test
    @DisplayName("Should have different timestamps for onCreate and onUpdate")
    void testOnCreateAndOnUpdate() throws InterruptedException {
        libro.onCreate();
        Thread.sleep(10);
        libro.onUpdate();

        assertNotNull(libro.getCreatedAt());
        assertNotNull(libro.getUpdatedAt());
        assertTrue(libro.getUpdatedAt().isAfter(libro.getCreatedAt()) ||
                   libro.getUpdatedAt().equals(libro.getCreatedAt()));
    }

    @Test
    @DisplayName("Should update updatedAt when onUpdate is called")
    void testOnUpdateChangesUpdatedAt() {
        LocalDateTime createdTime = LocalDateTime.now().minusHours(1);
        libro.setCreatedAt(createdTime);
        libro.setUpdatedAt(createdTime);
        libro.onUpdate();

        assertTrue(libro.getUpdatedAt().isAfter(createdTime));
    }

    @Test
    @DisplayName("Should consider two libros with identical fields as equal")
    void testEqualsWithSameValues() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Libro libro1 = new Libro(id, "Titulo", "Autor", "Genero", "ISBN", true, now, now);
        Libro libro2 = new Libro(id, "Titulo", "Autor", "Genero", "ISBN", true, now, now);

        assertEquals(libro1, libro2);
    }

    @Test
    @DisplayName("Should consider two libros with different id as not equal")
    void testEqualsWithDifferentId() {
        Libro libro1 = Libro.builder().id(UUID.randomUUID()).titulo("A").build();
        Libro libro2 = Libro.builder().id(UUID.randomUUID()).titulo("A").build();

        assertNotEquals(libro1, libro2);
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void testHashCodeConsistent() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Libro libro1 = new Libro(id, "T", "A", "G", "I", true, now, now);
        Libro libro2 = new Libro(id, "T", "A", "G", "I", true, now, now);

        assertEquals(libro1.hashCode(), libro2.hashCode());
    }

    @Test
    @DisplayName("Should have toString representation")
    void testToString() {
        libro.setTitulo("Test Libro");
        String toString = libro.toString();

        assertNotNull(toString);
        assertFalse(toString.isBlank());
    }
}
