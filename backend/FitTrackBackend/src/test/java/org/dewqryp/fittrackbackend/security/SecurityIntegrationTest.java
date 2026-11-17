package org.dewqryp.fittrackbackend.security;

import org.dewqryp.fittrackbackend.auth.jwt.JwtService;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "security.jwt.secret=dGVzdC1zZWNyZXQta2V5LXRlc3Qtc2VjcmV0LWtleS0xMjM0",
        "security.jwt.expiration-seconds=3600"
})
@AutoConfigureMockMvc
public class SecurityIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;


    @Test
    void shouldRejectProtectedEndpointWithoutToken() throws Exception {
        UUID uuid = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/users/" + uuid)).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowHealthEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/health")).andExpect(status().isOk());
    }

    @Test
    void shouldAllowProtectedEndpointWithValidToken() throws Exception {
        UUID uuid = UUID.randomUUID();
        User user = new User("Test", "Test", "test@example.com", "hashed-password");
        user.setId(uuid);
        String token = jwtService.generateJwtToken(user);
        when(userRepository.findById(uuid)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        mockMvc.perform(get("/api/v1/users/" + uuid).header("Authorization","Bearer " + token)).andExpect(status().isOk()).andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    void shouldReturnCurrentUserForValidToken() throws Exception {
        UUID uuid = UUID.randomUUID();
        User user = new User("Test", "Test", "test@example.com", "hashed-password");
        user.setId(uuid);
        String token = jwtService.generateJwtToken(user);
        when(userRepository.findById(uuid)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.id").value(user.getId().toString()));
        verify(userRepository).findByEmail(user.getEmail());
        verify(userRepository).findById(uuid);
    }

}
