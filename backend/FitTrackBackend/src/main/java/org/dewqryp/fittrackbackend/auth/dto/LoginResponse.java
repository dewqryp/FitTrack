package org.dewqryp.fittrackbackend.auth.dto;

public record LoginResponse(String accessToken, String refreshToken, long expiresIn) {
}
