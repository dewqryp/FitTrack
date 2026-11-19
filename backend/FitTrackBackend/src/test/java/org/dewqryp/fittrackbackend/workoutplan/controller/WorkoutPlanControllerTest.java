package org.dewqryp.fittrackbackend.workoutplan.controller;

import org.dewqryp.fittrackbackend.auth.security.JwtAuthenticationFilter;
import org.dewqryp.fittrackbackend.config.SecurityConfig;
import org.dewqryp.fittrackbackend.exercise.controller.ExerciseController;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workout.controller.WorkoutPlanController;
import org.dewqryp.fittrackbackend.workout.dto.WorkoutPlanCreateRequest;
import org.dewqryp.fittrackbackend.workout.dto.WorkoutPlanResponse;
import org.dewqryp.fittrackbackend.workout.service.WorkoutPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = WorkoutPlanController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(SecurityConfig.class)
public class WorkoutPlanControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private WorkoutPlanService workoutPlanService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnListOfWorkoutPlanForAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID workoutPlanId = UUID.randomUUID();
        WorkoutPlanResponse workoutPlanResponse = new WorkoutPlanResponse(workoutPlanId, "Workout", "Workout", userId, LocalDateTime.now());

        when(workoutPlanService.findAllByOwner(any(User.class))).thenReturn(List.of(workoutPlanResponse));

        mockMvc.perform(get("/api/v1/workout-plans"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$[0].id").value(workoutPlanId.toString()))
                .andExpect(jsonPath("$[0].name").value(workoutPlanResponse.name()))
                .andExpect(jsonPath("$[0].description").value(workoutPlanResponse.description()))
                .andExpect(jsonPath("$[0].ownerId").value(workoutPlanResponse.ownerId().toString()));
    }
    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnEmptyBodyForUserWithoutWorkoutPlan() throws Exception {
        when(workoutPlanService.findAllByOwner(any(User.class))).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/workout-plans"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("[]"));
    }
    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnWorkoutPlanById() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        WorkoutPlanResponse response = new WorkoutPlanResponse(workoutPlanId, "Workout", "Workout", userId, LocalDateTime.now());
        when(workoutPlanService.findById(any(UUID.class))).thenReturn(Optional.of(response));
        mockMvc.perform(get("/api/v1/workout-plans/" + workoutPlanId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(workoutPlanId.toString()))
                .andExpect(jsonPath("$.name").value(response.name()))
                .andExpect(jsonPath("$.description").value(response.description()))
                .andExpect(jsonPath("$.ownerId").value(response.ownerId().toString()));
    }
    @Test
    @WithMockUser(roles = "USER")
    void shouldNotReturnWorkoutPlanByNonExistentId() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        when(workoutPlanService.findById(any(UUID.class))).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/workout-plans/" + workoutPlanId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldCreateWorkoutPlan() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        WorkoutPlanResponse response = new WorkoutPlanResponse(workoutPlanId, "Workout", "Workout", userId, LocalDateTime.now());
        when(workoutPlanService.create(any(WorkoutPlanCreateRequest.class), any(User.class))).thenReturn(response);
        mockMvc.perform(post("/api/v1/workout-plans").contentType("application/json").content(objectMapper.writeValueAsString(response)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(workoutPlanId.toString()))
                .andExpect(jsonPath("$.name").value(response.name()))
                .andExpect(jsonPath("$.description").value(response.description()))
                .andExpect(jsonPath("$.ownerId").value(response.ownerId().toString()));


    }

    @Test
    void shouldNotAccessWithNonAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/v1/workout-plans")).andExpect(status().isForbidden());

    }


}
