package com.lexicon.prestamo.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    private static final String TEST_USERNAME = "testuser";

    @Test
    @DisplayName("Should generate a valid JWT token")
    void testGenerateTokenSuccess() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(token.contains("."));
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("Should generate valid tokens for repeated calls")
    void testGenerateTokenRepeatedCalls() {
        String token1 = jwtUtil.generateToken(TEST_USERNAME);
        String token2 = jwtUtil.generateToken(TEST_USERNAME);
        assertNotNull(token1);
        assertNotNull(token2);
        assertTrue(jwtUtil.validateToken(token1));
        assertTrue(jwtUtil.validateToken(token2));
        assertEquals(TEST_USERNAME, jwtUtil.getUsername(token1));
        assertEquals(TEST_USERNAME, jwtUtil.getUsername(token2));
    }

    @Test
    @DisplayName("Should generate different tokens for different usernames")
    void testGenerateTokenDifferentUsers() {
        String token1 = jwtUtil.generateToken("user1");
        String token2 = jwtUtil.generateToken("user2");
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Should validate a freshly generated token")
    void testValidateValidToken() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("Should reject invalid token format")
    void testValidateInvalidTokenFormat() {
        assertFalse(jwtUtil.validateToken("invalid.token.format"));
    }

    @Test
    @DisplayName("Should reject malformed token")
    void testValidateMalformedToken() {
        assertFalse(jwtUtil.validateToken("this-is-not-a-jwt"));
    }

    @Test
    @DisplayName("Should reject empty token")
    void testValidateEmptyToken() {
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    @DisplayName("Should reject null token")
    void testValidateNullToken() {
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    @DisplayName("Should reject tampered token")
    void testValidateTamperedToken() {
        String validToken = jwtUtil.generateToken(TEST_USERNAME);
        String tamperedToken = validToken.substring(0, validToken.length() - 1) + "X";
        assertFalse(jwtUtil.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("Should extract username from valid token")
    void testGetUsernameFromValidToken() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        String extractedUsername = jwtUtil.getUsername(token);
        assertNotNull(extractedUsername);
        assertEquals(TEST_USERNAME, extractedUsername);
    }

    @Test
    @DisplayName("Should return null for invalid token")
    void testGetUsernameFromInvalidToken() {
        assertNull(jwtUtil.getUsername("invalid-token"));
    }

    @Test
    @DisplayName("Should return null for malformed token")
    void testGetUsernameFromMalformedToken() {
        assertNull(jwtUtil.getUsername("not.a.jwt"));
    }

    @Test
    @DisplayName("Should return null for empty token")
    void testGetUsernameFromEmptyToken() {
        assertNull(jwtUtil.getUsername(""));
    }

    @Test
    @DisplayName("Should extract correct username from different users")
    void testGetUsernameMultipleUsers() {
        String[] usernames = {"alice", "bob", "charlie", "user@example.com"};
        for (String username : usernames) {
            String token = jwtUtil.generateToken(username);
            String extractedUsername = jwtUtil.getUsername(token);
            assertEquals(username, extractedUsername);
        }
    }

    @Test
    @DisplayName("Should return expiration time")
    void testGetExpiration() {
        long expiration = jwtUtil.getExpiration();
        assertTrue(expiration > 0);
        assertEquals(3600000, expiration);
    }

    @Test
    @DisplayName("Expiration should be consistent")
    void testExpirationConsistent() {
        long expiration1 = jwtUtil.getExpiration();
        long expiration2 = jwtUtil.getExpiration();
        assertEquals(expiration1, expiration2);
    }

    @Test
    @DisplayName("Complete flow: generate, validate, and extract username")
    void testCompleteJwtFlow() {
        String username = "integration_test_user";
        String token = jwtUtil.generateToken(username);
        boolean isValid = jwtUtil.validateToken(token);
        String extractedUsername = jwtUtil.getUsername(token);
        assertTrue(isValid);
        assertEquals(username, extractedUsername);
        assertTrue(token.contains("."));
    }

    @Test
    @DisplayName("Token with special characters in username")
    void testTokenWithSpecialCharactersUsername() {
        String username = "user+test@example.com";
        String token = jwtUtil.generateToken(username);
        boolean isValid = jwtUtil.validateToken(token);
        String extractedUsername = jwtUtil.getUsername(token);
        assertTrue(isValid);
        assertEquals(username, extractedUsername);
    }
}
