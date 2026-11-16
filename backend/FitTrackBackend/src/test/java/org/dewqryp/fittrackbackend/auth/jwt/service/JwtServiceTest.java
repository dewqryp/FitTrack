package org.dewqryp.fittrackbackend.auth.jwt.service;

import org.dewqryp.fittrackbackend.auth.jwt.JwtService;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {
    private JwtService jwtService;

    @BeforeEach
    public void setup() {
        String secret = Base64.getEncoder().encodeToString("test-secret-key-test-secret-key-1234".getBytes());
        jwtService = new JwtService(secret, 3600);
    }

    @Test
    void shouldGenerateCorrectToken() {
        UUID uuid = UUID.randomUUID();
        User user = new User("Test", "Test", "test@example.com", "password");
        user.setId(uuid);
        String token = jwtService.generateJwtToken(user);
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);

    }

    @Test
    void shouldExtractClaimsFromGeneratedToken() {
        UUID uuid = UUID.randomUUID();
        User user = new User("Test", "Test", "test@example.com", "hashed-password");
        user.setId(uuid);
        String token = jwtService.generateJwtToken(user);
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
        assertEquals(user.getId(), jwtService.extractUserId(token));
        assertEquals(user.getEmail(), jwtService.extractEmail(token));
        assertEquals(user.getRole(), jwtService.extractRole(token));

    }

    @Test
    void correctTokenShouldBeValid() {
        UUID uuid = UUID.randomUUID();
        User user = new User("Test", "Test", "test@example.com", "hashed-password");
        user.setId(uuid);
        String token = jwtService.generateJwtToken(user);
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
        assertTrue(jwtService.isTokenValid(token, user));

    }

    @Test
    void incorrectTokenShouldNotBeValid() {
        UUID uuid = UUID.randomUUID();
        User user = new User("Test", "Test", "test@example.com", "hashed-password");
        user.setId(uuid);
        String token = jwtService.generateJwtToken(user);
        user.setEmail("invalid@example.com");
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
        assertFalse(jwtService.isTokenValid(token, user));

    }

}
