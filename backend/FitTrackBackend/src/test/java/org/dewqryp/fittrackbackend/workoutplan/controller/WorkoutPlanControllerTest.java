package org.dewqryp.fittrackbackend.workoutplan.controller;

import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.auth.jwt.JwtService;
import org.dewqryp.fittrackbackend.config.SecurityConfig;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.user.repository.UserRepository;
import org.dewqryp.fittrackbackend.workout.controller.WorkoutPlanController;
import org.dewqryp.fittrackbackend.workout.dto.WorkoutPlanCreateRequest;
import org.dewqryp.fittrackbackend.workout.dto.WorkoutPlanResponse;
import org.dewqryp.fittrackbackend.workout.dto.WorkoutPlanUpdateRequest;
import org.dewqryp.fittrackbackend.workout.entity.WorkoutPlan;
import org.dewqryp.fittrackbackend.workout.service.WorkoutPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkoutPlanController.class)
@Import(SecurityConfig.class)
public class WorkoutPlanControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private WorkoutPlanService workoutPlanService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldReturnListOfWorkoutPlanForAuthenticatedUser() throws Exception {
        UUID  workoutPlanId = UUID.randomUUID();
        String email = "test@example.com";
        String token = UUID.randomUUID().toString();

        User user = new User(
                "Test",
                "User",
                email,
                "passwordHash"
        );
        user.setId(UUID.randomUUID());
        when(jwtService.extractEmail(token)).thenReturn(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(jwtService.isTokenValid(token, user))
                .thenReturn(true);

        WorkoutPlanResponse workoutPlanResponse = new WorkoutPlanResponse(workoutPlanId, "Workout", "Workout", user.getId(), LocalDateTime.now());

        when(workoutPlanService.findAllByOwner(user)).thenReturn(List.of(workoutPlanResponse));

        mockMvc.perform(get("/api/v1/workout-plans").header("Authorization","Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$[0].id").value(workoutPlanId.toString()))
                .andExpect(jsonPath("$[0].name").value(workoutPlanResponse.name()))
                .andExpect(jsonPath("$[0].description").value(workoutPlanResponse.description()))
                .andExpect(jsonPath("$[0].ownerId").value(workoutPlanResponse.ownerId().toString()));
        verify(workoutPlanService).findAllByOwner(user);
    }
    @Test
    void shouldNotReturnWorkoutPlanWhenOwnerDoesNotMatch() throws Exception {
        UUID  workoutPlanId = UUID.randomUUID();
        UUID aliceId = UUID.randomUUID();
        String email = "bob@example.com";
        String token = UUID.randomUUID().toString();

        User bob = new User(
                "Bob",
                "User",
                email,
                "passwordHash"
        );
        bob.setId(UUID.randomUUID());
        when(jwtService.extractEmail(token)).thenReturn(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(bob));

        when(jwtService.isTokenValid(token, bob))
                .thenReturn(true);


        when(workoutPlanService.findById(workoutPlanId, bob)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/workout-plans/{id}", workoutPlanId).header("Authorization","Bearer " + token))
                .andExpect(status().isNotFound());
        verify(workoutPlanService).findById(workoutPlanId, bob);
    }
    @Test
    void shouldReturnNotFoundWhenWorkoutPlanDoesNotExist() throws Exception {
        UUID  workoutPlanId = UUID.randomUUID();
        String email = "test@example.com";
        String token = UUID.randomUUID().toString();

        User user = new User(
                "Test",
                "User",
                email,
                "passwordHash"
        );
        user.setId(UUID.randomUUID());
        when(jwtService.extractEmail(token)).thenReturn(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(jwtService.isTokenValid(token, user))
                .thenReturn(true);

        when(workoutPlanService.findById(workoutPlanId, user)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/workout-plans/{id}", workoutPlanId).header("Authorization","Bearer " + token))
                .andExpect(status().isNotFound());
        verify(workoutPlanService).findById(workoutPlanId, user);
    }

    @Test
    void shouldDeleteWorkoutPlan() throws Exception {
        UUID  workoutPlanId = UUID.randomUUID();
        String email = "bob@example.com";
        String token = UUID.randomUUID().toString();

        User bob = new User(
                "Bob",
                "User",
                email,
                "passwordHash"
        );
        bob.setId(UUID.randomUUID());
        when(jwtService.extractEmail(token)).thenReturn(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(bob));

        when(jwtService.isTokenValid(token, bob))
                .thenReturn(true);
        WorkoutPlanResponse response = new WorkoutPlanResponse(workoutPlanId, "Workout", "Workout", bob.getId(), LocalDateTime.now());

        mockMvc.perform(delete("/api/v1/workout-plans/{id}", workoutPlanId).header("Authorization","Bearer " + token))
                .andExpect(status().isNoContent());
        verify(workoutPlanService).deleteWorkoutPlan(workoutPlanId, bob);
    }
    @Test
    void shouldNotDeleteSomeoneElseWorkoutPlan() throws Exception {
        UUID  workoutPlanId = UUID.randomUUID();
        UUID aliceId = UUID.randomUUID();
        String email = "bob@example.com";
        String token = UUID.randomUUID().toString();
        User alice = new User("Alice", "Yar", "alice@example.com", "hashed-password");
        User bob = new User(
                "Bob",
                "User",
                email,
                "passwordHash"
        );
        alice.setId(aliceId);
        bob.setId(UUID.randomUUID());
        when(jwtService.extractEmail(token)).thenReturn(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(bob));

        when(jwtService.isTokenValid(token, bob))
                .thenReturn(true);

        doThrow(new EntityNotFoundException("Workout plan not found"))
                .when(workoutPlanService)
                .deleteWorkoutPlan(workoutPlanId, bob);
        mockMvc.perform(delete("/api/v1/workout-plans/{id}", workoutPlanId).header("Authorization","Bearer " + token))
                .andExpect(status().isNotFound());
        verify(workoutPlanService).deleteWorkoutPlan(workoutPlanId, bob);
    }
    @Test
    void shouldNotDeleteNonExistentWorkoutPlan() throws Exception {
        UUID  workoutPlanId = UUID.randomUUID();
        String email = "bob@example.com";
        String token = UUID.randomUUID().toString();
        User bob = new User(
                "Bob",
                "User",
                email,
                "passwordHash"
        );
        bob.setId(UUID.randomUUID());
        when(jwtService.extractEmail(token)).thenReturn(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(bob));

        when(jwtService.isTokenValid(token, bob))
                .thenReturn(true);

        doThrow(new EntityNotFoundException("Workout plan not found"))
                .when(workoutPlanService)
                .deleteWorkoutPlan(workoutPlanId, bob);
        mockMvc.perform(delete("/api/v1/workout-plans/{id}", workoutPlanId).header("Authorization","Bearer " + token))
                .andExpect(status().isNotFound());
        verify(workoutPlanService).deleteWorkoutPlan(workoutPlanId, bob);
    }

    @Test
    void shouldUpdateWorkoutPlan() throws Exception {
        UUID  workoutPlanId = UUID.randomUUID();
        String email = "bob@example.com";
        String token = UUID.randomUUID().toString();
        User bob = new User(
                "Bob",
                "User",
                email,
                "passwordHash"
        );
        bob.setId(UUID.randomUUID());
        WorkoutPlanUpdateRequest request = new WorkoutPlanUpdateRequest("Update", "Update");
        WorkoutPlanResponse response = new WorkoutPlanResponse(workoutPlanId, request.name(), request.description(), bob.getId(), LocalDateTime.now());

        when(jwtService.extractEmail(token)).thenReturn(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(bob));

        when(jwtService.isTokenValid(token, bob))
                .thenReturn(true);

        when(workoutPlanService.updateWorkoutPlan(workoutPlanId, request,  bob)).thenReturn(response);
        mockMvc.perform(put("/api/v1/workout-plans/{id}", workoutPlanId).header("Authorization","Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.description").value(request.description()))
                .andExpect(jsonPath("$.id").value(workoutPlanId.toString()))
                .andExpect(jsonPath("$.ownerId").value(bob.getId().toString()));;
        verify(workoutPlanService).updateWorkoutPlan(workoutPlanId, request,  bob);
    }

    @Test
    void shouldNotUpdateSomeoneElseWorkoutPlan() throws Exception {
        UUID  workoutPlanId = UUID.randomUUID();
        String email = "bob@example.com";
        String token = UUID.randomUUID().toString();
        User bob = new User(
                "Bob",
                "User",
                email,
                "passwordHash"
        );
        bob.setId(UUID.randomUUID());
        WorkoutPlanUpdateRequest request = new WorkoutPlanUpdateRequest("Update", "Update");

        when(jwtService.extractEmail(token)).thenReturn(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(bob));

        when(jwtService.isTokenValid(token, bob))
                .thenReturn(true);
        doThrow(new EntityNotFoundException("Workout plan not found"))
                .when(workoutPlanService)
                .updateWorkoutPlan(workoutPlanId, request,  bob);
        mockMvc.perform(put("/api/v1/workout-plans/{id}", workoutPlanId).header("Authorization","Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        verify(workoutPlanService).updateWorkoutPlan(workoutPlanId, request,  bob);

    }

    @Test
    void shouldNotUpdateWorkoutPlanWithBlankFields() throws Exception {
        UUID  workoutPlanId = UUID.randomUUID();
        String email = "bob@example.com";
        String token = UUID.randomUUID().toString();
        User bob = new User(
                "Bob",
                "User",
                email,
                "passwordHash"
        );
        bob.setId(UUID.randomUUID());
        WorkoutPlanUpdateRequest request = new WorkoutPlanUpdateRequest(null, "Update");
        when(jwtService.extractEmail(token)).thenReturn(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(bob));

        when(jwtService.isTokenValid(token, bob))
                .thenReturn(true);
        mockMvc.perform(put("/api/v1/workout-plans/{id}", workoutPlanId).header("Authorization","Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(workoutPlanService, never())
                .updateWorkoutPlan(any(), any(), any());
    }

    @Test
    void shouldNotUpdateNonExistentWorkoutPlan() throws Exception {
        UUID  workoutPlanId = UUID.randomUUID();
        String email = "bob@example.com";
        String token = UUID.randomUUID().toString();
        User bob = new User(
                "Bob",
                "User",
                email,
                "passwordHash"
        );
        bob.setId(UUID.randomUUID());
        WorkoutPlanUpdateRequest request = new WorkoutPlanUpdateRequest("Update", "Update");

        when(jwtService.extractEmail(token)).thenReturn(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(bob));

        when(jwtService.isTokenValid(token, bob))
                .thenReturn(true);

        doThrow(new EntityNotFoundException("Workout plan not found")).when(workoutPlanService)
                .updateWorkoutPlan(workoutPlanId, request,  bob);

        mockMvc.perform(put("/api/v1/workout-plans/{id}", workoutPlanId).header("Authorization","Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(workoutPlanService)
                .updateWorkoutPlan(workoutPlanId, request, bob);

    }
}
