package org.dewqryp.fittrackbackend.exerciseset.controller;

import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.auth.jwt.JwtService;
import org.dewqryp.fittrackbackend.exercise.entity.Exercise;
import org.dewqryp.fittrackbackend.exerciseset.dto.ExerciseSetCreateRequest;
import org.dewqryp.fittrackbackend.exerciseset.dto.ExerciseSetResponse;
import org.dewqryp.fittrackbackend.exerciseset.dto.ExerciseSetUpdateRequest;
import org.dewqryp.fittrackbackend.exerciseset.entity.ExerciseSet;
import org.dewqryp.fittrackbackend.exerciseset.exception.ExerciseSetAlreadyExistsException;
import org.dewqryp.fittrackbackend.exerciseset.service.ExerciseSetService;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.user.repository.UserRepository;
import org.dewqryp.fittrackbackend.workout.entity.WorkoutPlan;
import org.dewqryp.fittrackbackend.workoutsession.entity.WorkoutSession;
import org.dewqryp.fittrackbackend.workoutsession.exceptions.WorkoutSessionIsAlreadyFinishedException;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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


    @Test
    void shouldReturnAllExerciseSets() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashedPassword");
        user.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout plan", "Workout plan", user);
        workoutPlan.setId(UUID.randomUUID());
        WorkoutSession session = new WorkoutSession(user, workoutPlan, "Notes");
        session.setId(sessionId);

        ExerciseSetResponse exerciseSetResponse1 = new ExerciseSetResponse(UUID.randomUUID(), sessionId, exerciseId, 1, 8, BigDecimal.valueOf(82.5), OffsetDateTime.now());
        ExerciseSetResponse exerciseSetResponse2 = new ExerciseSetResponse(UUID.randomUUID(), sessionId, exerciseId, 2, 8, BigDecimal.valueOf(82.5), OffsetDateTime.now());
        ExerciseSetResponse exerciseSetResponse3 = new ExerciseSetResponse(UUID.randomUUID(), sessionId, exerciseId, 3, 8, BigDecimal.valueOf(82.5), OffsetDateTime.now());
        when(exerciseSetService.getExerciseSets(eq(sessionId), any(User.class))).thenReturn(List.of(exerciseSetResponse1, exerciseSetResponse2, exerciseSetResponse3));

        mockMvc.perform(get("/api/v1/workout-sessions/{sessionId}/sets", sessionId).with(authenticatedAs(user)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(exerciseSetResponse1.id().toString()))
                .andExpect(jsonPath("$[1].id").value(exerciseSetResponse2.id().toString()))
                .andExpect(jsonPath("$[2].id").value(exerciseSetResponse3.id().toString()))
                .andExpect(jsonPath("$[0].setNumber").value(1))
                .andExpect(jsonPath("$[1].setNumber").value(2))
                .andExpect(jsonPath("$[2].setNumber").value(3))
                .andExpect(jsonPath("$[0].reps").value(8))
                .andExpect(jsonPath("$[0].weight").value(82.5));
        verify(exerciseSetService).getExerciseSets(eq(sessionId), any(User.class));

    }

    @Test
    void shouldReturnEmptyListWhenThereIsNoExerciseSets() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashedPassword");
        user.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout plan", "Workout plan", user);
        workoutPlan.setId(UUID.randomUUID());
        WorkoutSession session = new WorkoutSession(user, workoutPlan, "Notes");
        session.setId(sessionId);
        when(exerciseSetService.getExerciseSets(eq(sessionId), any(User.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/workout-sessions/{sessionId}/sets", sessionId).with(authenticatedAs(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
        verify(exerciseSetService).getExerciseSets(eq(sessionId), any(User.class));

    }

    @Test
    void shouldReturn404WhenWorkoutSessionIsNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        User user = new User("User", "User", "user@example.com", "hashedPassword");
        user.setId(userId);

        when(exerciseSetService.getExerciseSets(eq(sessionId), any(User.class)))
                .thenThrow(new EntityNotFoundException("Workout session not found"));

        mockMvc.perform(get("/api/v1/workout-sessions/{sessionId}/sets", sessionId)
                        .with(authenticatedAs(user)))
                .andExpect(status().isNotFound());

        verify(exerciseSetService).getExerciseSets(eq(sessionId), any(User.class));
    }

    @Test
    void shouldGetExerciseSetById() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashedPassword");
        user.setId(userId);
        ExerciseSetResponse response = new ExerciseSetResponse(UUID.randomUUID(), sessionId, exerciseId, 1, 8, BigDecimal.valueOf(82.5), OffsetDateTime.now());
        when(exerciseSetService.getExerciseSet(eq(response.id()), eq(sessionId), any(User.class))).thenReturn(Optional.of(response));
        mockMvc.perform(get("/api/v1/workout-sessions/{sessionId}/sets/{setId}", sessionId, response.id()).with(authenticatedAs(user)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
        verify(exerciseSetService).getExerciseSet(eq(response.id()), eq(sessionId), any(User.class));

    }
    @Test
    void shouldReturn404WhenExerciseSetNotFound() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID setId = UUID.randomUUID();
        User user = createUser();

        when(exerciseSetService.getExerciseSet(
                eq(setId), eq(sessionId), any(User.class)
        )).thenReturn(Optional.empty());

        mockMvc.perform(get(
                        "/api/v1/workout-sessions/{sessionId}/sets/{setId}",
                        sessionId, setId
                ).with(authenticatedAs(user)))
                .andExpect(status().isNotFound());

        verify(exerciseSetService)
                .getExerciseSet(eq(setId), eq(sessionId), any(User.class));
    }




    @Test
    void shouldUpdateExerciseSet() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashedPassword");
        user.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout plan", "Workout plan", user);
        workoutPlan.setId(UUID.randomUUID());
        WorkoutSession session = new WorkoutSession(user, workoutPlan, "Notes");
        session.setId(sessionId);
        Exercise exercise = new Exercise("Exercise", "Exercise", "Exercise", "Exercise", user);
        exercise.setId(exerciseId);
        ExerciseSet exerciseSet = new ExerciseSet(session, exercise, 1, 12, BigDecimal.valueOf(82.5));
        exerciseSet.setId(UUID.randomUUID());
        ExerciseSetUpdateRequest updateRequest = new ExerciseSetUpdateRequest(2, 8, BigDecimal.valueOf(100.0));
        ExerciseSetResponse response = new ExerciseSetResponse(exerciseSet.getId(), sessionId, exerciseId, updateRequest.setNumber(), updateRequest.reps(), updateRequest.weight(), OffsetDateTime.now());
        when(exerciseSetService.updateExerciseSet(eq(exerciseSet.getId()), eq(sessionId), eq(updateRequest), any(User.class))).thenReturn(response);
        mockMvc.perform(put("/api/v1/workout-sessions/{sessionId}/sets/{setId}", sessionId, exerciseSet.getId()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateRequest)).with(authenticatedAs(user)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(exerciseSet.getId().toString()))
                .andExpect(jsonPath("$.workoutSessionId").value(sessionId.toString()))
                .andExpect(jsonPath("$.exerciseId").value(exerciseId.toString()))
                .andExpect(jsonPath("$.setNumber").value(updateRequest.setNumber()))
                .andExpect(jsonPath("$.weight").value(updateRequest.weight()))
                .andExpect(jsonPath("$.reps").value(updateRequest.reps()));
        verify(exerciseSetService).updateExerciseSet(eq(exerciseSet.getId()), eq(sessionId), eq(updateRequest), any(User.class));
    }
    @Test
    void shouldReturn404WhenExerciseSetForUpdateNotFound() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID setId = UUID.randomUUID();
        User user = createUser();

        ExerciseSetUpdateRequest request =
                new ExerciseSetUpdateRequest(2, 8, BigDecimal.valueOf(100));

        when(exerciseSetService.updateExerciseSet(
                eq(setId), eq(sessionId), any(ExerciseSetUpdateRequest.class), any(User.class)
        )).thenThrow(new EntityNotFoundException("Exercise set not found"));

        mockMvc.perform(put("/api/v1/workout-sessions/{sessionId}/sets/{setId}", sessionId, setId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authenticatedAs(user)))
                .andExpect(status().isNotFound());

        verify(exerciseSetService).updateExerciseSet(
                eq(setId), eq(sessionId), any(ExerciseSetUpdateRequest.class), any(User.class)
        );
    }
    @Test
    void shouldThrow409WhenWorkoutSessionIsFinished() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID setId = UUID.randomUUID();
        User user = createUser();

        ExerciseSetUpdateRequest request =
                new ExerciseSetUpdateRequest(2, 8, BigDecimal.valueOf(100));

        when(exerciseSetService.updateExerciseSet(
                eq(setId), eq(sessionId), any(ExerciseSetUpdateRequest.class), any(User.class)
        )).thenThrow(new WorkoutSessionIsAlreadyFinishedException(
                "Workout session has already been finished"
        ));

        mockMvc.perform(put("/api/v1/workout-sessions/{sessionId}/sets/{setId}", sessionId, setId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authenticatedAs(user)))
                .andExpect(status().isConflict());

        verify(exerciseSetService).updateExerciseSet(
                eq(setId), eq(sessionId), any(ExerciseSetUpdateRequest.class), any(User.class)
        );
    }
    @Test
    void shouldReturn409WhenExerciseSetNumberAlreadyExists() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID setId = UUID.randomUUID();
        User user = createUser();

        ExerciseSetUpdateRequest request =
                new ExerciseSetUpdateRequest(2, 8, BigDecimal.valueOf(100));

        when(exerciseSetService.updateExerciseSet(
                eq(setId), eq(sessionId), any(ExerciseSetUpdateRequest.class), any(User.class)
        )).thenThrow(new ExerciseSetAlreadyExistsException(
                "Exercise set already exists"
        ));

        mockMvc.perform(put("/api/v1/workout-sessions/{sessionId}/sets/{setId}", sessionId, setId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authenticatedAs(user)))
                .andExpect(status().isConflict());

        verify(exerciseSetService).updateExerciseSet(
                eq(setId), eq(sessionId), any(ExerciseSetUpdateRequest.class), any(User.class)
        );
    }
    @Test
    void shouldReturn400WhenExerciseSetUpdateRequestIsInvalid() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID setId = UUID.randomUUID();
        User user = createUser();

        ExerciseSetUpdateRequest request =
                new ExerciseSetUpdateRequest(0, 0, BigDecimal.valueOf(-1));

        mockMvc.perform(put("/api/v1/workout-sessions/{sessionId}/sets/{setId}", sessionId, setId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authenticatedAs(user)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(exerciseSetService);
    }

    @Test
    void shouldDeleteExerciseSet() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashedPassword");
        user.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout plan", "Workout plan", user);
        workoutPlan.setId(UUID.randomUUID());
        WorkoutSession session = new WorkoutSession(user, workoutPlan, "Notes");
        session.setId(sessionId);
        Exercise exercise = new Exercise("Exercise", "Exercise", "Exercise", "Exercise", user);
        exercise.setId(exerciseId);
        ExerciseSet exerciseSet = new ExerciseSet(session, exercise, 1, 12, BigDecimal.valueOf(82.5));
        exerciseSet.setId(UUID.randomUUID());
        mockMvc.perform(delete("/api/v1/workout-sessions/{sessionId}/sets/{setId}",  sessionId, exerciseSet.getId()).with(authenticatedAs(user)))
                .andExpect(status().isNoContent());
        verify(exerciseSetService).deleteExerciseSet(eq(exerciseSet.getId()), eq(sessionId), any(User.class));
    }
    @Test
    void shouldReturn404WhenExerciseSetToDeleteNotFound() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID setId = UUID.randomUUID();
        User user = createUser();

        doThrow(new EntityNotFoundException("Exercise set not found"))
                .when(exerciseSetService)
                .deleteExerciseSet(eq(setId), eq(sessionId), any(User.class));

        mockMvc.perform(delete(
                        "/api/v1/workout-sessions/{sessionId}/sets/{setId}",
                        sessionId, setId
                ).with(authenticatedAs(user)))
                .andExpect(status().isNotFound());

        verify(exerciseSetService)
                .deleteExerciseSet(eq(setId), eq(sessionId), any(User.class));
    }

    @Test
    void shouldReturn409WhenTryingToDeleteExerciseSetWhenWorkoutSessionIsFinished() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID setId = UUID.randomUUID();
        User user = createUser();

        doThrow(new WorkoutSessionIsAlreadyFinishedException(
                "Workout session has already been finished"))
                .when(exerciseSetService)
                .deleteExerciseSet(eq(setId), eq(sessionId), any(User.class));

        mockMvc.perform(delete(
                        "/api/v1/workout-sessions/{sessionId}/sets/{setId}",
                        sessionId, setId
                ).with(authenticatedAs(user)))
                .andExpect(status().isConflict());

        verify(exerciseSetService)
                .deleteExerciseSet(eq(setId), eq(sessionId), any(User.class));
    }
}
