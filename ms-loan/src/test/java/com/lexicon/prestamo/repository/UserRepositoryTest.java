package com.lexicon.prestamo.repository;

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

import com.lexicon.prestamo.entity.User;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .passwordHash("$2a$10$hashedpassword")
                .build();
    }

    @Test
    @DisplayName("Should find user by username")
    void testFindByUsernameSuccess() {
        entityManager.persistAndFlush(testUser);
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    @DisplayName("Should return empty Optional when user not found")
    void testFindByUsernameNotFound() {
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");
        assertTrue(foundUser.isEmpty());
    }

    @Test
    @DisplayName("Should save user and retrieve by id")
    void testSaveAndRetrieveUser() {
        User savedUser = userRepository.save(testUser);
        User retrievedUser = userRepository.findById(savedUser.getId()).orElse(null);

        assertNotNull(retrievedUser);
        assertEquals(testUser.getUsername(), retrievedUser.getUsername());
    }

    @Test
    @DisplayName("Should assign UUID on save")
    void testUUIDAssignedOnSave() {
        assertNull(testUser.getId());
        User savedUser = userRepository.save(testUser);
        assertNotNull(savedUser.getId());
        assertTrue(savedUser.getId() instanceof UUID);
    }

    @Test
    @DisplayName("Should enforce username uniqueness constraint")
    void testUsernameUniqueConstraint() {
        User user1 = User.builder().username("duplicate").passwordHash("hash1").build();
        User user2 = User.builder().username("duplicate").passwordHash("hash2").build();

        userRepository.save(user1);
        entityManager.flush();

        assertThrows(Exception.class, () -> {
            userRepository.save(user2);
            entityManager.flush();
        });
    }
}
