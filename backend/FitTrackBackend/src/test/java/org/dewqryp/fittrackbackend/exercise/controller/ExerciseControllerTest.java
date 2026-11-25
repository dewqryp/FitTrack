
package org.dewqryp.fittrackbackend.exercise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.auth.security.JwtAuthenticationFilter;
import org.dewqryp.fittrackbackend.exercise.dto.ExerciseCreateRequest;
import org.dewqryp.fittrackbackend.exercise.dto.ExerciseResponse;
import org.dewqryp.fittrackbackend.exercise.dto.ExerciseUpdateRequest;
import org.dewqryp.fittrackbackend.exercise.entity.Exercise;
import org.dewqryp.fittrackbackend.exercise.exceptions.ExerciseInUseException;
import org.dewqryp.fittrackbackend.exercise.service.ExerciseService;
import org.dewqryp.fittrackbackend.user.entity.Role;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ExerciseController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(ExerciseControllerTest.TestSecurityConfig.class)
class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ExerciseService exerciseService;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
    }

    private User createUser(Role role) {
        User user = new User(
                "User",
                "User",
                "user@example.com",
                "hashed-password"
        );
        user.setRole(role);
        return user;
    }

    private void authenticate(Role role) {
        User user = createUser(role);

        var authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnListOfExercisesForAuthenticatedUser() throws Exception {
        authenticate(Role.USER);

        UUID exerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ExerciseResponse response = new ExerciseResponse(
                exerciseId,
                "Test",
                "Test",
                "Test",
                "Test",
                userId
        );

        when(exerciseService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/exercises"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(exerciseId.toString()))
                .andExpect(jsonPath("$[0].name").value("Test"));

        verify(exerciseService).findAll();
    }

    @Test
    void shouldReturnExerciseById() throws Exception {
        authenticate(Role.USER);

        UUID exerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ExerciseResponse response = new ExerciseResponse(
                exerciseId,
                "Test",
                "Test",
                "Test",
                "Test",
                userId
        );

        when(exerciseService.findById(exerciseId))
                .thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/v1/exercises/{id}", exerciseId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(exerciseId.toString()))
                .andExpect(jsonPath("$.name").value("Test"));

        verify(exerciseService).findById(exerciseId);
    }

    @Test
    void shouldReturn404WhenExerciseDoesNotExist() throws Exception {
        authenticate(Role.USER);

        UUID exerciseId = UUID.randomUUID();

        when(exerciseService.findById(exerciseId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/exercises/{id}", exerciseId))
                .andExpect(status().isNotFound());

        verify(exerciseService).findById(exerciseId);
    }

    @Test
    void shouldCreateExerciseForTrainer() throws Exception {
        authenticate(Role.TRAINER);

        ExerciseCreateRequest request = new ExerciseCreateRequest(
                "Bench Press",
                "Chest exercise",
                "Chest",
                "Barbell"
        );

        UUID userId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();

        ExerciseResponse response = new ExerciseResponse(
                exerciseId,
                "Bench Press",
                "Chest exercise",
                "Chest",
                "Barbell",
                userId
        );

        when(exerciseService.create(any(ExerciseCreateRequest.class), any(User.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(exerciseId.toString()))
                .andExpect(jsonPath("$.name").value("Bench Press"));

        verify(exerciseService).create(any(ExerciseCreateRequest.class), any(User.class));
    }

    @Test
    void shouldForbidExerciseCreationForRegularUser() throws Exception {
        authenticate(Role.USER);

        ExerciseCreateRequest request = new ExerciseCreateRequest(
                "Bench Press",
                "Chest exercise",
                "Chest",
                "Barbell"
        );

        mockMvc.perform(post("/api/v1/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(exerciseService);
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        authenticate(Role.TRAINER);

        ExerciseCreateRequest request = new ExerciseCreateRequest(
                "",
                "",
                "Chest",
                "Barbell"
        );

        mockMvc.perform(post("/api/v1/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(exerciseService);
    }

    @Test
    void shouldUpdateExerciseForTrainer() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        User trainer = createUser(Role.TRAINER);
        trainer.setId(UUID.randomUUID());
        authenticate(Role.TRAINER);
        ExerciseUpdateRequest request = new ExerciseUpdateRequest("Updated", "Updated", "Updated", "Updated");
        Exercise exercise = new Exercise("Test", "Test", "Test", "Test", trainer);
        ExerciseResponse response = new ExerciseResponse(exerciseId, request.name(), request.description(), request.muscleGroup(), request.equipment(), trainer.getId());
        exercise.setId(exerciseId);
        when(exerciseService.update(exerciseId, request)).thenReturn(response);

        mockMvc.perform(put("/api/v1/exercises/{id}", exerciseId).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(exerciseId.toString()))
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.description").value(request.description()))
                .andExpect(jsonPath("$.muscleGroup").value(request.muscleGroup()))
                .andExpect(jsonPath("$.equipment").value(request.equipment()))
                .andExpect(jsonPath("$.createdBy").value(exercise.getCreatedBy().getId().toString()));
        verify(exerciseService).update(exerciseId, request);
    }
    @Test
    void shouldUpdateExerciseForAdmin() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        User admin = createUser(Role.ADMIN);
        admin.setId(UUID.randomUUID());
        authenticate(Role.ADMIN);
        ExerciseUpdateRequest request = new ExerciseUpdateRequest("Updated", "Updated", "Updated", "Updated");
        Exercise exercise = new Exercise("Test", "Test", "Test", "Test", admin);
        ExerciseResponse response = new ExerciseResponse(exerciseId, request.name(), request.description(), request.muscleGroup(), request.equipment(), admin.getId());
        exercise.setId(exerciseId);
        when(exerciseService.update(exerciseId, request)).thenReturn(response);

        mockMvc.perform(put("/api/v1/exercises/{id}", exerciseId).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(exerciseId.toString()))
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.description").value(request.description()))
                .andExpect(jsonPath("$.muscleGroup").value(request.muscleGroup()))
                .andExpect(jsonPath("$.equipment").value(request.equipment()))
                .andExpect(jsonPath("$.createdBy").value(exercise.getCreatedBy().getId().toString()));
        verify(exerciseService).update(exerciseId, request);
    }

    @Test
    void shouldNotUpdateExerciseForTrainerWhenExerciseDoesNotExist() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        authenticate(Role.TRAINER);
        ExerciseUpdateRequest request = new ExerciseUpdateRequest("Updated", "Updated", "Updated", "Updated");
        doThrow(EntityNotFoundException.class).when(exerciseService).update(exerciseId, request);
        mockMvc.perform(put("/api/v1/exercises/{id}", exerciseId).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        verify(exerciseService).update(exerciseId, request);
    }
    @Test
    void shouldNotUpdateExerciseForTrainerWithInvalidUpdateRequest() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        User trainer = createUser(Role.TRAINER);
        trainer.setId(UUID.randomUUID());
        authenticate(Role.TRAINER);
        ExerciseUpdateRequest request = new ExerciseUpdateRequest(null, "Updated", "Updated", "Updated");
        mockMvc.perform(put("/api/v1/exercises/{id}", exerciseId).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(exerciseService, never()).update(exerciseId, request);
    }

    @Test
    void shouldDeleteExerciseForTrainer() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        User trainer = createUser(Role.TRAINER);
        trainer.setId(UUID.randomUUID());
        authenticate(Role.TRAINER);
        mockMvc.perform(delete("/api/v1/exercises/{id}", exerciseId))
                .andExpect(status().isNoContent());

        verify(exerciseService).deleteById(exerciseId);
    }

    @Test
    void shouldDeleteExerciseForAdmin() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        User admin = createUser(Role.ADMIN);
        admin.setId(UUID.randomUUID());
        authenticate(Role.ADMIN);
        mockMvc.perform(delete("/api/v1/exercises/{id}", exerciseId))
                .andExpect(status().isNoContent());

        verify(exerciseService).deleteById(exerciseId);
    }
    @Test
    void shouldNotDeleteExerciseForUser() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        User user = createUser(Role.USER);
        user.setId(UUID.randomUUID());
        authenticate(Role.USER);
        mockMvc.perform(delete("/api/v1/exercises/{id}", exerciseId))
                .andExpect(status().isForbidden());

        verify(exerciseService, never()).deleteById(exerciseId);
    }

    @Test
    void shouldNotDeleteNonExistingExercise() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        User trainer = createUser(Role.TRAINER);
        trainer.setId(UUID.randomUUID());
        authenticate(Role.TRAINER);
        doThrow(EntityNotFoundException.class).when(exerciseService).deleteById(exerciseId);
        mockMvc.perform(delete("/api/v1/exercises/{id}", exerciseId))
                .andExpect(status().isNotFound());

    }

    @Test
    void shouldNotDeleteExerciseThatUsedInWorkoutPlans() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        User trainer = createUser(Role.TRAINER);
        trainer.setId(UUID.randomUUID());
        authenticate(Role.TRAINER);
        doThrow(ExerciseInUseException.class).when(exerciseService).deleteById(exerciseId);
        mockMvc.perform(delete("/api/v1/exercises/{id}", exerciseId))
                .andExpect(status().isConflict());

    }

    @Test
    void shouldCreateExerciseWithTrainerRole() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        User trainer = createUser(Role.TRAINER);
        trainer.setId(UUID.randomUUID());
        authenticate(Role.TRAINER);
        ExerciseCreateRequest createRequest = new ExerciseCreateRequest("Exercise", "Exercise", "Exercise", "Exercise");
        ExerciseResponse response = new ExerciseResponse(exerciseId, createRequest.name(), createRequest.description(), createRequest.muscleGroup(), createRequest.equipment(), trainer.getId());
        when(exerciseService.create(eq(createRequest), any(User.class))).thenReturn(response);
        mockMvc.perform(post("/api/v1/exercises").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(objectMapper.writeValueAsString(response)));
        verify(exerciseService).create(eq(createRequest), any(User.class));
    }
    @Test
    void shouldCreateExerciseWithAdminRole() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        User admin = createUser(Role.ADMIN);
        admin.setId(UUID.randomUUID());
        authenticate(Role.ADMIN);
        ExerciseCreateRequest createRequest = new ExerciseCreateRequest("Exercise", "Exercise", "Exercise", "Exercise");
        ExerciseResponse response = new ExerciseResponse(exerciseId, createRequest.name(), createRequest.description(), createRequest.muscleGroup(), createRequest.equipment(), admin.getId());
        when(exerciseService.create(eq(createRequest), any(User.class))).thenReturn(response);
        mockMvc.perform(post("/api/v1/exercises").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(objectMapper.writeValueAsString(response)));
        verify(exerciseService).create(eq(createRequest), any(User.class));
    }
    @Test
    void shouldNotCreateExerciseWithUserRole() throws Exception {
        User user = createUser(Role.USER);
        user.setId(UUID.randomUUID());
        authenticate(Role.USER);
        ExerciseCreateRequest createRequest = new ExerciseCreateRequest("Exercise", "Exercise", "Exercise", "Exercise");
        mockMvc.perform(post("/api/v1/exercises").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
        verify(exerciseService, never()).create(eq(createRequest), any(User.class));
    }

}
