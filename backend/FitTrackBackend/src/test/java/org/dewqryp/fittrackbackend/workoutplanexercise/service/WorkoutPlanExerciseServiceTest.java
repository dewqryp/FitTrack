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
import org.junit.jupiter.api.BeforeEach;
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

    private User user;
    private WorkoutPlan workoutPlan;
    private UUID workoutPlanId;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        workoutPlanId = UUID.randomUUID();

        user = new User("Test", "Test", "Test", "password");
        user.setId(userId);

        workoutPlan = new WorkoutPlan("Workout", "Test", user);
        workoutPlan.setId(workoutPlanId);
    }

    @Test
    void shouldAddExerciseToWorkoutPlan() {
        UUID exerciseId = UUID.randomUUID();
        Exercise exercise = new Exercise("Test", "Test", "Test", "Test", user);
        exercise.setId(exerciseId);
        WorkoutPlanExerciseCreateRequest workoutPlanExerciseCreateRequest = new WorkoutPlanExerciseCreateRequest(exercise.getId(), 1, 12, 90, 2);
        WorkoutPlanExercise workoutPlanExercise = new WorkoutPlanExercise(workoutPlan, exercise, workoutPlanExerciseCreateRequest.orderIndex(), workoutPlanExerciseCreateRequest.sets(), workoutPlanExerciseCreateRequest.reps(), workoutPlanExerciseCreateRequest.restSeconds());
        when(workoutPlanRepository.findById(workoutPlan.getId())).thenReturn(Optional.of(workoutPlan));
        when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));
        when(workoutPlanExerciseRepository.save(any(WorkoutPlanExercise.class))).thenReturn(workoutPlanExercise);
        WorkoutPlanExerciseResponse workoutPlanExerciseResponse = workoutPlanExerciseService.addExerciseToWorkoutPlan(workoutPlan.getId(), workoutPlanExerciseCreateRequest);
        assertAll(
            () -> assertEquals(workoutPlanExerciseCreateRequest.exerciseId(), workoutPlanExerciseResponse.exerciseId()),
            () -> assertEquals(workoutPlanExerciseCreateRequest.orderIndex(), workoutPlanExerciseResponse.orderIndex()),
            () -> assertEquals(workoutPlanExerciseCreateRequest.reps(), workoutPlanExerciseResponse.reps()),
            () -> assertEquals(workoutPlanExerciseCreateRequest.restSeconds(), workoutPlanExerciseResponse.restSeconds()),
            () -> assertEquals(workoutPlanExerciseCreateRequest.sets(), workoutPlanExerciseResponse.sets()),
            () -> assertEquals(workoutPlanExercise.getId(), workoutPlanExerciseResponse.id())
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
                () -> assertEquals(exerciseId, savedEntity.getExercise().getId()),
                () -> assertEquals(workoutPlanId, savedEntity.getWorkoutPlan().getId())
        );
        verify(workoutPlanRepository).findById(workoutPlan.getId());
        verify(exerciseRepository).findById(exercise.getId());
        verify(workoutPlanExerciseRepository).save(any(WorkoutPlanExercise.class));
    }

    @Test
    void shouldThrowExceptionWhenWorkoutPlanNotFound(){
        when(workoutPlanRepository.findById(workoutPlanId)).thenReturn(Optional.empty());
        WorkoutPlanExerciseCreateRequest workoutPlanExerciseCreateRequest = new WorkoutPlanExerciseCreateRequest(UUID.randomUUID(), 1, 12, 90, 2);
        assertThrows(IllegalArgumentException.class, () -> workoutPlanExerciseService.addExerciseToWorkoutPlan(workoutPlanId, workoutPlanExerciseCreateRequest));
        verifyNoInteractions(workoutPlanExerciseRepository);
        verify(workoutPlanRepository).findById(workoutPlanId);
        verifyNoInteractions(exerciseRepository);
    }

    @Test
    void shouldThrowExceptionWhenExerciseNotFound(){
        UUID exerciseId = UUID.randomUUID();
        WorkoutPlanExerciseCreateRequest workoutPlanExerciseCreateRequest = new WorkoutPlanExerciseCreateRequest(exerciseId, 1, 12, 90, 2);
        when(workoutPlanRepository.findById(workoutPlanId)).thenReturn(Optional.of(workoutPlan));
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> workoutPlanExerciseService.addExerciseToWorkoutPlan(workoutPlanId, workoutPlanExerciseCreateRequest));
        verifyNoInteractions(workoutPlanExerciseRepository);
    }

}
