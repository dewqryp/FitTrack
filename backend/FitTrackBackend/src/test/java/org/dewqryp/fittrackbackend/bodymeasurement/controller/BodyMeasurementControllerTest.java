package org.dewqryp.fittrackbackend.bodymeasurement.controller;

import org.dewqryp.fittrackbackend.auth.jwt.JwtService;
import org.dewqryp.fittrackbackend.bodymeasurement.dto.BodyMeasurementCreateRequest;
import org.dewqryp.fittrackbackend.bodymeasurement.dto.BodyMeasurementResponse;
import org.dewqryp.fittrackbackend.bodymeasurement.repository.BodyMeasurementRepository;
import org.dewqryp.fittrackbackend.bodymeasurement.service.BodyMeasurementService;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BodyMeasurementController.class)
public class BodyMeasurementControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private BodyMeasurementService  bodyMeasurementService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;

    private User createUser() {
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(UUID.randomUUID());
        return user;
    }

    private RequestPostProcessor authenticatedAs(User user){
        return authentication(new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }


    @Test
    void shouldCreateBodyMeasurement() throws Exception {
        UUID bodyMeasurementId = UUID.randomUUID();
        BodyMeasurementCreateRequest createRequest = new BodyMeasurementCreateRequest(BigDecimal.valueOf(57.5), 12, "Notes");
        User user = createUser();
        BodyMeasurementResponse response = new BodyMeasurementResponse(bodyMeasurementId, createRequest.weight(), createRequest.bodyFatPercentage(), OffsetDateTime.now(), createRequest.notes());
        when(bodyMeasurementService.createBodyMeasurement(eq(createRequest), any(User.class))).thenReturn(response);
        mockMvc.perform(post("/api/v1/body-measurements").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(createRequest)).with(authenticatedAs(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(bodyMeasurementId.toString()))
                .andExpect(jsonPath("$.weight").value(57.5))
                .andExpect(jsonPath("$.bodyFatPercentage").value(12))
                .andExpect(jsonPath("$.notes").value("Notes"));
        verify(bodyMeasurementService).createBodyMeasurement(eq(createRequest), any(User.class));
    }

    @Test
    void shouldReturn400WhenWeightIsInvalid() throws Exception {
        User user = createUser();

        BodyMeasurementCreateRequest request =
                new BodyMeasurementCreateRequest(BigDecimal.ZERO, 12, "Notes");

        mockMvc.perform(post("/api/v1/body-measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authenticatedAs(user)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bodyMeasurementService);
    }
    @Test
    void shouldReturnAllBodyMeasurements() throws Exception {
        User user = createUser();

        BodyMeasurementResponse response1 = new BodyMeasurementResponse(
                UUID.randomUUID(), BigDecimal.valueOf(57.5), 12,
                OffsetDateTime.now(), "First measurement"
        );

        BodyMeasurementResponse response2 = new BodyMeasurementResponse(
                UUID.randomUUID(), BigDecimal.valueOf(58.0), 13,
                OffsetDateTime.now(), "Second measurement"
        );

        when(bodyMeasurementService.getAllBodyMeasurements(any(User.class)))
                .thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/v1/body-measurements")
                        .with(authenticatedAs(user)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(response1.id().toString()))
                .andExpect(jsonPath("$[0].notes").value("First measurement"))
                .andExpect(jsonPath("$[1].id").value(response2.id().toString()))
                .andExpect(jsonPath("$[1].notes").value("Second measurement"));

        verify(bodyMeasurementService).getAllBodyMeasurements(any(User.class));
    }
    @Test
    void shouldReturnEmptyListWhenNoBodyMeasurementsExist() throws Exception {
        User user = createUser();

        when(bodyMeasurementService.getAllBodyMeasurements(any(User.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/body-measurements")
                        .with(authenticatedAs(user)))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(bodyMeasurementService).getAllBodyMeasurements(any(User.class));
    }

    
}
