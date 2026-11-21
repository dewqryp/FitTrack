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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        WorkoutPlanExerciseCreateRequest workoutPlanExerciseCreateRequest = new WorkoutPlanExerciseCreateRequest(exerciseId, 1, 12, 90, 2);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout", "Test", user);
        workoutPlan.setId(workoutPlanId);
        Exercise exercise = new Exercise("Test", "Test", "Test", "Test", user);
        exercise.setId(exerciseId);
        WorkoutPlanExercise workoutPlanExercise = new WorkoutPlanExercise(workoutPlan, exercise, workoutPlanExerciseCreateRequest.orderIndex(), workoutPlanExerciseCreateRequest.sets(), workoutPlanExerciseCreateRequest.reps(), workoutPlanExerciseCreateRequest.restSeconds());
        workoutPlanExercise.setId(workoutPlanExerciseId);
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
        ArgumentCaptor<WorkoutPlanExercise> captor =
                ArgumentCaptor.forClass(WorkoutPlanExercise.class);

        verify(workoutPlanExerciseRepository).save(captor.capture());

        WorkoutPlanExercise savedEntity = captor.getValue();
        assertAll(
                () -> assertEquals(exercise, savedEntity.getExercise()),
                () -> assertNull(savedEntity.getId()),
                () -> assertEquals(workoutPlanExerciseCreateRequest.orderIndex(), savedEntity.getOrderIndex()),
                () -> assertEquals(workoutPlanExerciseCreateRequest.reps(), savedEntity.getReps()),
                () -> assertEquals(workoutPlanExerciseCreateRequest.restSeconds(), savedEntity.getRestSeconds()),
                () -> assertEquals(workoutPlanExerciseCreateRequest.sets(), savedEntity.getSets()),
                () -> assertEquals(workoutPlan, savedEntity.getWorkoutPlan())
        );
        verify(workoutPlanRepository).findById(workoutPlanId);
        verify(exerciseRepository).findById(exerciseId);
        verify(workoutPlanExerciseRepository).save(any(WorkoutPlanExercise.class));
    }

    @Test
    void shouldThrowExceptionWhenWorkoutPlanNotFound(){
        UUID workoutPlanId = UUID.randomUUID();
        when(workoutPlanRepository.findById(workoutPlanId)).thenReturn(Optional.empty());
        WorkoutPlanExerciseCreateRequest workoutPlanExerciseCreateRequest = new WorkoutPlanExerciseCreateRequest(UUID.randomUUID(), 1, 12, 90, 2);
        assertThrows(IllegalArgumentException.class, () -> workoutPlanExerciseService.addExerciseToWorkoutPlan(UUID.randomUUID(), workoutPlanExerciseCreateRequest));
        verifyNoInteractions(workoutPlanExerciseRepository);
        verifyNoInteractions(workoutPlanRepository);
        verifyNoInteractions(exerciseRepository);
    }

}
