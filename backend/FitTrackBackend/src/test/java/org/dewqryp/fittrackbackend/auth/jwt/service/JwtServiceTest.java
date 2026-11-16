package org.dewqryp.fittrackbackend.auth.jwt.service;

import org.dewqryp.fittrackbackend.auth.jwt.JwtService;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

}
