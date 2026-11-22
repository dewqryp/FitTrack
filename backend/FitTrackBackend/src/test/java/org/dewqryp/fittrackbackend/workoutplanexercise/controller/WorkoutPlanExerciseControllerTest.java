package org.dewqryp.fittrackbackend.workoutplanexercise.controller;

import org.dewqryp.fittrackbackend.auth.security.JwtAuthenticationFilter;
import org.dewqryp.fittrackbackend.workout.controller.WorkoutPlanController;
import org.dewqryp.fittrackbackend.workoutplanexercises.controller.WorkoutPlanExerciseController;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseCreateRequest;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseResponse;
import org.dewqryp.fittrackbackend.workoutplanexercises.service.WorkoutPlanExerciseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        WorkoutPlanExerciseCreateRequest createRequest = new WorkoutPlanExerciseCreateRequest(exerciseId, 4, 12, 90, 1);
        WorkoutPlanExerciseResponse response = new WorkoutPlanExerciseResponse(workoutPlanExerciseId, exerciseId, createRequest.sets(), createRequest.reps(), createRequest.restSeconds(), createRequest.orderIndex());
        when(workoutPlanExerciseService.addExerciseToWorkoutPlan(workoutPlanId, createRequest)).thenReturn(response);
        mockMvc.perform(post("/api/workout-plans/{workoutPlanId}/exercises", workoutPlanId).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workoutPlanExerciseId.toString()))
                .andExpect(jsonPath("$.exerciseId").value(exerciseId.toString()))
                .andExpect(jsonPath("$.sets").value(response.sets()))
                .andExpect(jsonPath("$.reps").value(response.reps()))
                .andExpect(jsonPath("$.restSeconds").value(response.restSeconds()))
                .andExpect(jsonPath("$.restSeconds").value(response.restSeconds()));
        verify(workoutPlanExerciseService)
                .addExerciseToWorkoutPlan(workoutPlanId, createRequest);
    }
}
