package com.lexicon.prestamo.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User Entity Tests")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    @DisplayName("Should create User with no-arg constructor")
    void testNoArgConstructor() {
        User newUser = new User();
        assertNotNull(newUser);
        assertNull(newUser.getId());
        assertNull(newUser.getUsername());
    }

    @Test
    @DisplayName("Should create User with builder")
    void testBuilder() {
        User builtUser = User.builder()
                .username("testuser")
                .passwordHash("hashedpassword")
                .build();

        assertNotNull(builtUser);
        assertEquals("testuser", builtUser.getUsername());
        assertEquals("hashedpassword", builtUser.getPasswordHash());
    }

    @Test
    @DisplayName("Should set and get all fields")
    void testSettersAndGetters() {
        UUID id = UUID.randomUUID();
        user.setId(id);
        user.setUsername("testuser");
        user.setPasswordHash("$2a$10$hash");

        assertEquals(id, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("$2a$10$hash", user.getPasswordHash());
    }

    @Test
    @DisplayName("Should update updatedAt when onUpdate is called")
    void testOnUpdateChangesUpdatedAt() {
        LocalDateTime createdTime = LocalDateTime.now().minusHours(1);
        user.setCreatedAt(createdTime);
        user.setUpdatedAt(createdTime);
        user.onUpdate();
        assertTrue(user.getUpdatedAt().isAfter(createdTime));
    }

    @Test
    @DisplayName("Should have toString representation")
    void testToString() {
        user.setUsername("testuser");
        assertNotNull(user.toString());
        assertFalse(user.toString().isBlank());
    }
}
