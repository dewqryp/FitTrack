package org.dewqryp.fittrackbackend.exerciseset.controller;

import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.auth.jwt.JwtService;
import org.dewqryp.fittrackbackend.exerciseset.dto.ExerciseSetCreateRequest;
import org.dewqryp.fittrackbackend.exerciseset.dto.ExerciseSetResponse;
import org.dewqryp.fittrackbackend.exerciseset.exception.ExerciseSetAlreadyExistsException;
import org.dewqryp.fittrackbackend.exerciseset.service.ExerciseSetService;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.user.repository.UserRepository;
import org.dewqryp.fittrackbackend.workoutsession.exceptions.WorkoutSessionIsAlreadyFinishedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExerciseSetController.class)
class ExerciseSetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExerciseSetService exerciseSetService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private User createUser() {
        User user = new User(
                "Test",
                "User",
                "test@example.com",
                "hashed-password"
        );
        user.setId(UUID.randomUUID());
        return user;
    }

    private RequestPostProcessor authenticatedAs(User user) {
        return authentication(
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
    }

    @Test
    void shouldCreateExerciseSet() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        UUID exerciseSetId = UUID.randomUUID();

        User user = createUser();

        ExerciseSetCreateRequest request = new ExerciseSetCreateRequest(
                exerciseId,
                1,
                8,
                BigDecimal.valueOf(60)
        );

        ExerciseSetResponse response = new ExerciseSetResponse(
                exerciseSetId,
                sessionId,
                exerciseId,
                1,
                8,
                BigDecimal.valueOf(60),
                OffsetDateTime.now()
        );

        when(exerciseSetService.createExerciseSet(
                eq(sessionId),
                any(ExerciseSetCreateRequest.class),
                any(User.class)
        )).thenReturn(response);

        mockMvc.perform(post(
                        "/api/v1/workout-sessions/{sessionId}/sets",
                        sessionId
                )
                        .with(authenticatedAs(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(exerciseSetId.toString()))
                .andExpect(jsonPath("$.workoutSessionId").value(sessionId.toString()))
                .andExpect(jsonPath("$.exerciseId").value(exerciseId.toString()))
                .andExpect(jsonPath("$.setNumber").value(1))
                .andExpect(jsonPath("$.reps").value(8))
                .andExpect(jsonPath("$.weight").value(60));

        verify(exerciseSetService).createExerciseSet(
                eq(sessionId),
                any(ExerciseSetCreateRequest.class),
                any(User.class)
        );
    }

    @Test
    void shouldReturn404WhenWorkoutSessionNotFound() throws Exception {
        UUID sessionId = UUID.randomUUID();
        User user = createUser();

        ExerciseSetCreateRequest request = new ExerciseSetCreateRequest(
                UUID.randomUUID(),
                1,
                8,
                BigDecimal.valueOf(60)
        );

        when(exerciseSetService.createExerciseSet(
                eq(sessionId),
                any(ExerciseSetCreateRequest.class),
                any(User.class)
        )).thenThrow(new EntityNotFoundException("Workout session not found"));

        mockMvc.perform(post(
                        "/api/v1/workout-sessions/{sessionId}/sets",
                        sessionId
                )
                        .with(authenticatedAs(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(exerciseSetService).createExerciseSet(
                eq(sessionId),
                any(ExerciseSetCreateRequest.class),
                any(User.class)
        );
    }

    @Test
    void shouldReturn409WhenWorkoutSessionIsFinished() throws Exception {
        UUID sessionId = UUID.randomUUID();
        User user = createUser();

        ExerciseSetCreateRequest request = new ExerciseSetCreateRequest(
                UUID.randomUUID(),
                1,
                8,
                BigDecimal.valueOf(60)
        );

        when(exerciseSetService.createExerciseSet(
                eq(sessionId),
                any(ExerciseSetCreateRequest.class),
                any(User.class)
        )).thenThrow(new WorkoutSessionIsAlreadyFinishedException(
                "Workout session has already been finished"
        ));

        mockMvc.perform(post(
                        "/api/v1/workout-sessions/{sessionId}/sets",
                        sessionId
                )
                        .with(authenticatedAs(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(exerciseSetService).createExerciseSet(
                eq(sessionId),
                any(ExerciseSetCreateRequest.class),
                any(User.class)
        );
    }

    @Test
    void shouldReturn409WhenExerciseSetAlreadyExists() throws Exception {
        UUID sessionId = UUID.randomUUID();
        User user = createUser();

        ExerciseSetCreateRequest request = new ExerciseSetCreateRequest(
                UUID.randomUUID(),
                1,
                8,
                BigDecimal.valueOf(60)
        );

        when(exerciseSetService.createExerciseSet(
                eq(sessionId),
                any(ExerciseSetCreateRequest.class),
                any(User.class)
        )).thenThrow(new ExerciseSetAlreadyExistsException(
                "Exercise set already exists"
        ));

        mockMvc.perform(post(
                        "/api/v1/workout-sessions/{sessionId}/sets",
                        sessionId
                )
                        .with(authenticatedAs(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(exerciseSetService).createExerciseSet(
                eq(sessionId),
                any(ExerciseSetCreateRequest.class),
                any(User.class)
        );
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        UUID sessionId = UUID.randomUUID();
        User user = createUser();

        ExerciseSetCreateRequest request = new ExerciseSetCreateRequest(
                null,
                0,
                0,
                null
        );

        mockMvc.perform(post(
                        "/api/v1/workout-sessions/{sessionId}/sets",
                        sessionId
                )
                        .with(authenticatedAs(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(exerciseSetService);
    }
    
}
