package org.dewqryp.fittrackbackend.auth.jwt;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import org.dewqryp.fittrackbackend.user.entity.Role;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import io.jsonwebtoken.security.Keys;



@Service
public class JwtService {

    private final long expirationSeconds;
    private final SecretKey secretKey;

    public JwtService(@Value("${security.jwt.secret}") String secret,@Value("${security.jwt.expiration-seconds}") long expirationSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationSeconds = expirationSeconds;
    }


    public String generateJwtToken(User user) {

        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(expirationSeconds);
        return Jwts.builder().subject(user.getId().toString()).claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
        .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("email", String.class);
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject());
    }

    public Role extractRole(String token) {
        return Role.valueOf(Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class));
    }

}
