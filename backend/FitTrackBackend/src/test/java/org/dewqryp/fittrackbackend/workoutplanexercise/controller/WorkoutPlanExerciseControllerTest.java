package org.dewqryp.fittrackbackend.workoutplanexercise.controller;

import org.dewqryp.fittrackbackend.auth.security.JwtAuthenticationFilter;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
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
                .andExpect(jsonPath("$.orderIndex").value(response.orderIndex()));
        verify(workoutPlanExerciseService)
                .addExerciseToWorkoutPlan(workoutPlanId, createRequest);
    }

    @Test
    void shouldGetExercisesByWorkoutPlanId() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        UUID workoutPlanExerciseId = UUID.randomUUID();
        UUID anotherWorkoutPlanExerciseId = UUID.randomUUID();
        WorkoutPlanExerciseResponse response1 = new WorkoutPlanExerciseResponse(workoutPlanExerciseId, UUID.randomUUID(), 4, 12, 90, 1);
        WorkoutPlanExerciseResponse response2 = new WorkoutPlanExerciseResponse(anotherWorkoutPlanExerciseId, UUID.randomUUID(), 4, 12, 90, 1);
        when(workoutPlanExerciseService.getExercisesByWorkoutPlanId(workoutPlanId)).thenReturn(List.of(response1, response2));
        mockMvc.perform(get("/api/workout-plans/{workoutPlanId}/exercises", workoutPlanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].exerciseId").value(response1.exerciseId().toString()))
                .andExpect(jsonPath("$[1].exerciseId").value(response2.exerciseId().toString()))
                .andExpect(jsonPath("$[0].id").value(workoutPlanExerciseId.toString()))
                .andExpect(jsonPath("$[1].id").value(anotherWorkoutPlanExerciseId.toString()));
        verify(workoutPlanExerciseService).getExercisesByWorkoutPlanId(workoutPlanId);

    }

    @Test
    void shouldUpdateWorkoutPlanExercise() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        UUID workoutPlanExerciseId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();

        WorkoutPlanExerciseUpdateRequest updateRequest = new WorkoutPlanExerciseUpdateRequest(3, 5, 120, 2);
        WorkoutPlanExerciseResponse response = new WorkoutPlanExerciseResponse(workoutPlanExerciseId, exerciseId, updateRequest.sets(), updateRequest.reps(), updateRequest.restSeconds(), updateRequest.orderIndex());
        when(workoutPlanExerciseService.updateWorkoutPlanExercise(workoutPlanId, workoutPlanExerciseId, updateRequest)).thenReturn(response);

        mockMvc.perform(put("/api/workout-plans/{workoutPlanId}/exercises/{id}",  workoutPlanId, workoutPlanExerciseId).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workoutPlanExerciseId.toString()))
                .andExpect(jsonPath("$.exerciseId").value(exerciseId.toString()))
                .andExpect(jsonPath("$.sets").value(response.sets()))
                .andExpect(jsonPath("$.reps").value(response.reps()))
                .andExpect(jsonPath("$.restSeconds").value(response.restSeconds()))
                .andExpect(jsonPath("$.orderIndex").value(response.orderIndex()));
        verify(workoutPlanExerciseService).updateWorkoutPlanExercise(workoutPlanId, workoutPlanExerciseId, updateRequest);

    }

    @Test
    void shouldDeleteWorkoutPlanExercise() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        UUID workoutPlanExerciseId = UUID.randomUUID();
        mockMvc.perform(delete("/api/workout-plans/{workoutPlanId}/exercises/{id}",   workoutPlanId, workoutPlanExerciseId))
                .andExpect(status().isOk());
        verify(workoutPlanExerciseService).deleteWorkoutPlanExercise(workoutPlanId, workoutPlanExerciseId);
    }

    @Test
    void shouldReturnBadRequestWhenSetsIsZero() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        WorkoutPlanExerciseCreateRequest createRequest = new WorkoutPlanExerciseCreateRequest(exerciseId, 0, 12, 90, 1);
        mockMvc.perform(post("/api/workout-plans/{workoutPlanId}/exercises", workoutPlanId).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(workoutPlanExerciseService);

    }

    @Test
    void shouldReturnBadRequestWhenExerciseIdIsNull() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        WorkoutPlanExerciseCreateRequest createRequest = new WorkoutPlanExerciseCreateRequest(null, 4, 12, 90, 1);
        mockMvc.perform(post("/api/workout-plans/{workoutPlanId}/exercises", workoutPlanId).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(workoutPlanExerciseService);

    }

}
