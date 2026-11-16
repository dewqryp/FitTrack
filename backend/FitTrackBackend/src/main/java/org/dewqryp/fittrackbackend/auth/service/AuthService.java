package org.dewqryp.fittrackbackend.auth.service;

import org.dewqryp.fittrackbackend.auth.dto.LoginRequest;
import org.dewqryp.fittrackbackend.auth.dto.LoginResponse;
import org.dewqryp.fittrackbackend.auth.exceptions.InvalidCredentialsException;
import org.dewqryp.fittrackbackend.auth.jwt.JwtService;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final long expirationSeconds;


    public AuthService(JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder, @Value("${security.jwt.expiration-seconds}") long expirationSeconds) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.expirationSeconds = expirationSeconds;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
        if(!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())){
            throw new InvalidCredentialsException("Invalid username or password");
        }
        return new LoginResponse(jwtService.generateJwtToken(user), "Bearer", expirationSeconds);
    }
}
