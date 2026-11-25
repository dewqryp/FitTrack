package org.dewqryp.fittrackbackend.workoutplanexercise.controller;

import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.auth.security.JwtAuthenticationFilter;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workout.controller.WorkoutPlanController;
import org.dewqryp.fittrackbackend.workoutplanexercises.controller.WorkoutPlanExerciseController;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseCreateRequest;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseResponse;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseUpdateRequest;
import org.dewqryp.fittrackbackend.workoutplanexercises.service.WorkoutPlanExerciseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WorkoutPlanExerciseController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        ))
public class WorkoutPlanExerciseControllerTest {
    @MockitoBean
    private WorkoutPlanExerciseService workoutPlanExerciseService;
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldAddExerciseToWorkoutPlan() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        UUID workoutPlanExerciseId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        WorkoutPlanExerciseCreateRequest createRequest = new WorkoutPlanExerciseCreateRequest(exerciseId, 4, 12, 90, 1);
        WorkoutPlanExerciseResponse response = new WorkoutPlanExerciseResponse(workoutPlanExerciseId, exerciseId, createRequest.sets(), createRequest.reps(), createRequest.restSeconds(), createRequest.orderIndex());
        when(workoutPlanExerciseService.addExerciseToWorkoutPlan(
                eq(workoutPlanId),
                any(WorkoutPlanExerciseCreateRequest.class),
                any(User.class)
        )).thenReturn(response);
        mockMvc.perform(post("/api/v1/workout-plans/{workoutPlanId}/exercises", workoutPlanId).with(authentication(new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(workoutPlanExerciseId.toString()))
                .andExpect(jsonPath("$.exerciseId").value(exerciseId.toString()))
                .andExpect(jsonPath("$.sets").value(response.sets()))
                .andExpect(jsonPath("$.reps").value(response.reps()))
                .andExpect(jsonPath("$.restSeconds").value(response.restSeconds()))
                .andExpect(jsonPath("$.orderIndex").value(response.orderIndex()));
        verify(workoutPlanExerciseService)
                .addExerciseToWorkoutPlan(eq(workoutPlanId),
                        any(WorkoutPlanExerciseCreateRequest.class),
                        any(User.class));
    }

    @Test
    void shouldGetExercisesByWorkoutPlanId() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        UUID workoutPlanExerciseId = UUID.randomUUID();
        UUID anotherWorkoutPlanExerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        WorkoutPlanExerciseResponse response1 = new WorkoutPlanExerciseResponse(workoutPlanExerciseId, UUID.randomUUID(), 4, 12, 90, 1);
        WorkoutPlanExerciseResponse response2 = new WorkoutPlanExerciseResponse(anotherWorkoutPlanExerciseId, UUID.randomUUID(), 4, 12, 90, 1);
        when(workoutPlanExerciseService.getExercisesByWorkoutPlanId(eq(workoutPlanId), any(User.class))).thenReturn(List.of(response1, response2));
        mockMvc.perform(get("/api/v1/workout-plans/{workoutPlanId}/exercises", workoutPlanId).with(authentication(new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].exerciseId").value(response1.exerciseId().toString()))
                .andExpect(jsonPath("$[1].exerciseId").value(response2.exerciseId().toString()))
                .andExpect(jsonPath("$[0].id").value(workoutPlanExerciseId.toString()))
                .andExpect(jsonPath("$[1].id").value(anotherWorkoutPlanExerciseId.toString()));
        verify(workoutPlanExerciseService).getExercisesByWorkoutPlanId(eq(workoutPlanId), any(User.class));

    }

    @Test
    void shouldUpdateWorkoutPlanExercise() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        UUID workoutPlanExerciseId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        WorkoutPlanExerciseUpdateRequest updateRequest = new WorkoutPlanExerciseUpdateRequest(3, 5, 120, 2);
        WorkoutPlanExerciseResponse response = new WorkoutPlanExerciseResponse(workoutPlanExerciseId, exerciseId, updateRequest.sets(), updateRequest.reps(), updateRequest.restSeconds(), updateRequest.orderIndex());
        when(workoutPlanExerciseService.updateWorkoutPlanExercise(eq(workoutPlanId), eq(workoutPlanExerciseId), eq(updateRequest), any(User.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/workout-plans/{workoutPlanId}/exercises/{id}",  workoutPlanId, workoutPlanExerciseId)
                .with(authentication(new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workoutPlanExerciseId.toString()))
                .andExpect(jsonPath("$.exerciseId").value(exerciseId.toString()))
                .andExpect(jsonPath("$.sets").value(response.sets()))
                .andExpect(jsonPath("$.reps").value(response.reps()))
                .andExpect(jsonPath("$.restSeconds").value(response.restSeconds()))
                .andExpect(jsonPath("$.orderIndex").value(response.orderIndex()));
        verify(workoutPlanExerciseService).updateWorkoutPlanExercise(eq(workoutPlanId), eq(workoutPlanExerciseId), eq(updateRequest), any(User.class));

    }

    @Test
    void shouldDeleteWorkoutPlanExercise() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        UUID workoutPlanExerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com",  "hashed-password");
        user.setId(userId);
        mockMvc.perform(delete("/api/v1/workout-plans/{workoutPlanId}/exercises/{id}",   workoutPlanId, workoutPlanExerciseId)
                .with(authentication(new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk());
        verify(workoutPlanExerciseService).deleteWorkoutPlanExercise(eq(workoutPlanId), eq(workoutPlanExerciseId), any(User.class));
    }

    @Test
    void shouldNotDeleteSomeoneElseWorkoutPlanExercise() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        UUID workoutPlanExerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com",  "hashed-password");
        user.setId(userId);
        doThrow(EntityNotFoundException.class).when(workoutPlanExerciseService).deleteWorkoutPlanExercise(eq(workoutPlanId), eq(workoutPlanExerciseId), any(User.class));
        mockMvc.perform(delete("/api/v1/workout-plans/{workoutPlanId}/exercises/{id}",   workoutPlanId, workoutPlanExerciseId)
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isNotFound());
        verify(workoutPlanExerciseService).deleteWorkoutPlanExercise(eq(workoutPlanId), eq(workoutPlanExerciseId), any(User.class));


    }



    @Test
    void shouldReturnBadRequestWhenSetsIsZero() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        WorkoutPlanExerciseCreateRequest createRequest = new WorkoutPlanExerciseCreateRequest(exerciseId, 0, 12, 90, 1);
        mockMvc.perform(post("/api/v1/workout-plans/{workoutPlanId}/exercises", workoutPlanId).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(workoutPlanExerciseService);

    }

    @Test
    void shouldReturnBadRequestWhenExerciseIdIsNull() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        WorkoutPlanExerciseCreateRequest createRequest = new WorkoutPlanExerciseCreateRequest(null, 4, 12, 90, 1);
        mockMvc.perform(post("/api/v1/workout-plans/{workoutPlanId}/exercises", workoutPlanId).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(workoutPlanExerciseService);

    }

    @Test
    void shouldGetWorkoutPlanExerciseById() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        UUID workoutPlanExerciseId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        User user = new User("Test", "User", "test@example.com", "password");
        user.setId(ownerId);

        WorkoutPlanExerciseResponse response =
                new WorkoutPlanExerciseResponse(
                        workoutPlanExerciseId,
                        exerciseId,
                        4,
                        12,
                        90,
                        1
                );

        when(workoutPlanExerciseService.getWorkoutPlanExerciseById(
                eq(workoutPlanExerciseId),
                eq(workoutPlanId),
                any(User.class)
        )).thenReturn(Optional.of(response));

        mockMvc.perform(get(
                        "/api/v1/workout-plans/{workoutPlanId}/exercises/{id}",
                        workoutPlanId,
                        workoutPlanExerciseId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workoutPlanExerciseId.toString()))
                .andExpect(jsonPath("$.exerciseId").value(exerciseId.toString()))
                .andExpect(jsonPath("$.sets").value(4))
                .andExpect(jsonPath("$.reps").value(12))
                .andExpect(jsonPath("$.restSeconds").value(90))
                .andExpect(jsonPath("$.orderIndex").value(1));

        verify(workoutPlanExerciseService).getWorkoutPlanExerciseById(
                eq(workoutPlanExerciseId),
                eq(workoutPlanId),
                any(User.class)
        );
    }
    @Test
    void shouldNotGetSomeoneElseWorkoutPlanExercise() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        UUID workoutPlanExerciseId = UUID.randomUUID();
        User user = new User("Test", "User", "test@example.com", "password");
        user.setId(UUID.randomUUID());
        when(workoutPlanExerciseService.getWorkoutPlanExerciseById(eq(workoutPlanExerciseId), eq(workoutPlanId), any(User.class))).thenReturn(java.util.Optional.empty());
        mockMvc.perform(get("/api/v1/workout-plans/{workoutPlanId}/exercises/{id}", workoutPlanId, workoutPlanExerciseId))
                .andExpect(status().isNotFound());

        verify(workoutPlanExerciseService).getWorkoutPlanExerciseById(eq(workoutPlanExerciseId), eq(workoutPlanId), any(User.class));
    }

    @Test
    void shouldNotGetNonExistentWorkoutPlanExercise() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        UUID workoutPlanExerciseId = UUID.randomUUID();

        User user = new User("Test", "User", "test@example.com", "password");
        user.setId(UUID.randomUUID());

        when(workoutPlanExerciseService.getWorkoutPlanExerciseById(
                eq(workoutPlanExerciseId),
                eq(workoutPlanId),
                any(User.class)
        )).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get(
                        "/api/v1/workout-plans/{workoutPlanId}/exercises/{id}",
                        workoutPlanId,
                        workoutPlanExerciseId
                ))
                .andExpect(status().isNotFound());

        verify(workoutPlanExerciseService).getWorkoutPlanExerciseById(
                eq(workoutPlanExerciseId),
                eq(workoutPlanId),
                any(User.class)
        );
    }

}