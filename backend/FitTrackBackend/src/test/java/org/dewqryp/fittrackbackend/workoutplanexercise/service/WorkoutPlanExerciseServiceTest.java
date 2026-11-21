package org.dewqryp.fittrackbackend.workoutplanexercise.service;

import org.dewqryp.fittrackbackend.exercise.entity.Exercise;
import org.dewqryp.fittrackbackend.exercise.repository.ExerciseRepository;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workout.entity.WorkoutPlan;
import org.dewqryp.fittrackbackend.workout.repository.WorkoutPlanRepository;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseCreateRequest;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseResponse;
import org.dewqryp.fittrackbackend.workoutplanexercises.entity.WorkoutPlanExercise;
import org.dewqryp.fittrackbackend.workoutplanexercises.repository.WorkoutPlanExerciseRepository;
import org.dewqryp.fittrackbackend.workoutplanexercises.service.WorkoutPlanExerciseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WorkoutPlanExerciseServiceTest {
    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private WorkoutPlanRepository workoutPlanRepository;
    @Mock
    private WorkoutPlanExerciseRepository workoutPlanExerciseRepository;
    @InjectMocks
    private WorkoutPlanExerciseService workoutPlanExerciseService;

    @Test
    void shouldAddExerciseToWorkoutPlan() {
        UUID userId = UUID.randomUUID();
        UUID workoutPlanId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        UUID workoutPlanExerciseId = UUID.randomUUID();
        User user = new User("Test", "Test", "Test", "hashed-password");
        user.setId(userId);
        WorkoutPlanExerciseCreateRequest workoutPlanExerciseCreateRequest = new WorkoutPlanExerciseCreateRequest(exerciseId, 1, 12, 90, 100);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout", "Test", user);
        workoutPlan.setId(workoutPlanId);
        Exercise exercise = new Exercise("Test", "Test", "Test", "Test", user);
        WorkoutPlanExercise workoutPlanExercise = new WorkoutPlanExercise(workoutPlan, exercise, workoutPlanExerciseCreateRequest.orderIndex(), workoutPlanExerciseCreateRequest.sets(), workoutPlanExerciseCreateRequest.reps(), workoutPlanExerciseCreateRequest.restSeconds());
        workoutPlanExercise.setId(workoutPlanExerciseId);
        exercise.setId(exerciseId);
        when(workoutPlanRepository.findById(workoutPlanId)).thenReturn(Optional.of(workoutPlan));
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));
        when(workoutPlanExerciseRepository.save(any(WorkoutPlanExercise.class))).thenReturn(workoutPlanExercise);
        WorkoutPlanExerciseResponse workoutPlanExerciseResponse = workoutPlanExerciseService.addExerciseToWorkoutPlan(workoutPlanId, workoutPlanExerciseCreateRequest);
        assertAll(
            () -> assertEquals(workoutPlanExerciseCreateRequest.exerciseId(), workoutPlanExerciseResponse.exerciseId()),
            () -> assertEquals(workoutPlanExerciseCreateRequest.orderIndex(), workoutPlanExerciseResponse.orderIndex()),
            () -> assertEquals(workoutPlanExerciseCreateRequest.reps(), workoutPlanExerciseResponse.reps()),
            () -> assertEquals(workoutPlanExerciseCreateRequest.restSeconds(), workoutPlanExerciseResponse.restSeconds()),
            () -> assertEquals(workoutPlanExerciseCreateRequest.sets(), workoutPlanExerciseResponse.sets()),
            () -> assertEquals(workoutPlanExerciseId, workoutPlanExerciseResponse.id())
                );
    }

}
