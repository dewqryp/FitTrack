package org.dewqryp.fittrackbackend.workoutplanexercise.service;

import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.exercise.entity.Exercise;
import org.dewqryp.fittrackbackend.exercise.repository.ExerciseRepository;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workout.entity.WorkoutPlan;
import org.dewqryp.fittrackbackend.workout.repository.WorkoutPlanRepository;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseCreateRequest;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseResponse;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseUpdateRequest;
import org.dewqryp.fittrackbackend.workoutplanexercises.entity.WorkoutPlanExercise;
import org.dewqryp.fittrackbackend.workoutplanexercises.repository.WorkoutPlanExerciseRepository;
import org.dewqryp.fittrackbackend.workoutplanexercises.service.WorkoutPlanExerciseService;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.security.autoconfigure.SecurityProperties;

import java.util.Collections;
import java.util.List;
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
        when(workoutPlanRepository.findByIdAndOwner_Id(workoutPlan.getId(), user.getId())).thenReturn(Optional.of(workoutPlan));
        when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));
        when(workoutPlanExerciseRepository.save(any(WorkoutPlanExercise.class))).thenReturn(workoutPlanExercise);
        WorkoutPlanExerciseResponse workoutPlanExerciseResponse = workoutPlanExerciseService.addExerciseToWorkoutPlan(workoutPlan.getId(), workoutPlanExerciseCreateRequest, user);
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
        verify(workoutPlanRepository).findByIdAndOwner_Id(workoutPlan.getId(), user.getId());
        verify(exerciseRepository).findById(exercise.getId());
        verify(workoutPlanExerciseRepository).save(any(WorkoutPlanExercise.class));
    }

    @Test
    void shouldThrowExceptionWhenWorkoutPlanNotFound(){
        when(workoutPlanRepository.findByIdAndOwner_Id(workoutPlanId, user.getId())).thenReturn(Optional.empty());
        WorkoutPlanExerciseCreateRequest workoutPlanExerciseCreateRequest = new WorkoutPlanExerciseCreateRequest(UUID.randomUUID(), 1, 12, 90, 2);
        assertThrows(EntityNotFoundException.class, () -> workoutPlanExerciseService.addExerciseToWorkoutPlan(workoutPlanId, workoutPlanExerciseCreateRequest, user));
        verifyNoInteractions(workoutPlanExerciseRepository);
        verify(workoutPlanRepository).findByIdAndOwner_Id(workoutPlanId, user.getId());
        verifyNoInteractions(exerciseRepository);
    }

    @Test
    void shouldThrowExceptionWhenExerciseNotFound(){
        UUID exerciseId = UUID.randomUUID();
        WorkoutPlanExerciseCreateRequest workoutPlanExerciseCreateRequest = new WorkoutPlanExerciseCreateRequest(exerciseId, 1, 12, 90, 2);
        when(workoutPlanRepository.findByIdAndOwner_Id(workoutPlanId, user.getId())).thenReturn(Optional.of(workoutPlan));
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> workoutPlanExerciseService.addExerciseToWorkoutPlan(workoutPlanId, workoutPlanExerciseCreateRequest, user));
        verifyNoInteractions(workoutPlanExerciseRepository);
    }

    @Test
    void shouldFindExercisesByWorkoutPlanId(){
        UUID workoutPlanExerciseId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        WorkoutPlanExercise workoutPlanExercise = getWorkoutPlanExercise(exerciseId, workoutPlanExerciseId);
        when(workoutPlanRepository.existsByIdAndOwner_Id(workoutPlanId, user.getId())).thenReturn(true);
        when(workoutPlanExerciseRepository.findAllByWorkoutPlanIdOrderByOrderIndexAsc(workoutPlanId)).thenReturn(List.of(workoutPlanExercise));
        List<WorkoutPlanExerciseResponse> responses = workoutPlanExerciseService.getExercisesByWorkoutPlanId(workoutPlanId, user);
        assertAll(
                () -> assertEquals(1, responses.size()),
                () -> assertEquals(workoutPlanExercise.getId(), responses.getFirst().id()),
                () -> assertEquals(workoutPlanExercise.getExercise().getId(), responses.getFirst().exerciseId()),
                () -> assertEquals(workoutPlanExercise.getSets(), responses.getFirst().sets()),
                () -> assertEquals(workoutPlanExercise.getReps(), responses.getFirst().reps()),
                () -> assertEquals(workoutPlanExercise.getRestSeconds(), responses.getFirst().restSeconds()),
                () -> assertEquals(workoutPlanExercise.getOrderIndex(), responses.getFirst().orderIndex())
        );
        verify(workoutPlanRepository).existsByIdAndOwner_Id(workoutPlanId, user.getId());
        verify(workoutPlanExerciseRepository)
                .findAllByWorkoutPlanIdOrderByOrderIndexAsc(workoutPlanId);
    }

    private @NonNull WorkoutPlanExercise getWorkoutPlanExercise(UUID exerciseId, UUID workoutPlanExerciseId) {
        Exercise exercise = new Exercise("Test", "Test", "Test", "Test", user);
        exercise.setId(exerciseId);
        WorkoutPlanExerciseCreateRequest workoutPlanExerciseCreateRequest = new WorkoutPlanExerciseCreateRequest(exercise.getId(), 1, 12, 90, 2);
        WorkoutPlanExercise workoutPlanExercise = new WorkoutPlanExercise(workoutPlan, exercise, workoutPlanExerciseCreateRequest.orderIndex(), workoutPlanExerciseCreateRequest.sets(), workoutPlanExerciseCreateRequest.reps(), workoutPlanExerciseCreateRequest.restSeconds());
        workoutPlanExercise.setId(workoutPlanExerciseId);
        return workoutPlanExercise;
    }

    @Test
    void shouldThrowExceptionWhenWorkoutPlanDoesNotExist(){
        when(workoutPlanRepository.existsByIdAndOwner_Id(workoutPlanId, user.getId())).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> workoutPlanExerciseService.getExercisesByWorkoutPlanId(workoutPlanId, user));
        verify(workoutPlanRepository).existsByIdAndOwner_Id(workoutPlanId, user.getId());
        verifyNoInteractions(workoutPlanExerciseRepository);
    }

    @Test
    void shouldReturnEmptyListWhenWorkoutPlanHasNoExercises(){
        when(workoutPlanRepository.existsByIdAndOwner_Id(workoutPlanId, user.getId())).thenReturn(true);
        when(workoutPlanExerciseRepository.findAllByWorkoutPlanIdOrderByOrderIndexAsc(workoutPlanId)).thenReturn(Collections.emptyList());
        List<WorkoutPlanExerciseResponse> responses =  workoutPlanExerciseService.getExercisesByWorkoutPlanId(workoutPlanId, user);
        assertTrue(responses.isEmpty());
        verify(workoutPlanRepository).existsByIdAndOwner_Id(workoutPlanId, user.getId());

        verify(workoutPlanExerciseRepository)
                .findAllByWorkoutPlanIdOrderByOrderIndexAsc(workoutPlanId);
    }

    @Test
    void shouldUpdateWorkoutPlanExercise(){
        UUID exerciseId = UUID.randomUUID();
        UUID workoutPlanExerciseId = UUID.randomUUID();
        Exercise exercise = new Exercise("Test", "Test", "Test", "Test", user);
        exercise.setId(exerciseId);
        WorkoutPlanExercise workoutPlanExercise = new WorkoutPlanExercise(workoutPlan, exercise, 1, 12, 90, 2);
        workoutPlanExercise.setId(workoutPlanExerciseId);
        when(workoutPlanExerciseRepository.findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, user.getId())).thenReturn(Optional.of(workoutPlanExercise));
        WorkoutPlanExerciseUpdateRequest updateRequest = new WorkoutPlanExerciseUpdateRequest(2, 3, 50, 4);
        when(workoutPlanExerciseRepository.save(any(WorkoutPlanExercise.class))).thenAnswer(invocation -> invocation.getArgument(0));
        WorkoutPlanExerciseResponse response = workoutPlanExerciseService.updateWorkoutPlanExercise(workoutPlanId, workoutPlanExerciseId, updateRequest, user);
        assertAll(
                () -> assertEquals(updateRequest.reps(), response.reps()),
                () -> assertEquals(updateRequest.sets(), response.sets()),
                () -> assertEquals(updateRequest.restSeconds(), response.restSeconds()),
                () -> assertEquals(workoutPlanExercise.getId(), response.id()),
                () -> assertEquals(updateRequest.orderIndex(), response.orderIndex())
        );
        verify(workoutPlanExerciseRepository).findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, user.getId());
        verify(workoutPlanExerciseRepository).save(any(WorkoutPlanExercise.class));
    }

    @Test
    void shouldThrowExceptionWhenWorkoutPlanExerciseDoesNotExist(){
        UUID workoutPlanExerciseId = UUID.randomUUID();
        when(workoutPlanExerciseRepository.findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, user.getId())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> workoutPlanExerciseService.updateWorkoutPlanExercise(workoutPlanId,workoutPlanExerciseId, new WorkoutPlanExerciseUpdateRequest(1, 2, 3, 4), user));
        verify(workoutPlanExerciseRepository).findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId,  user.getId());
        verify(workoutPlanExerciseRepository,  never()).save(any(WorkoutPlanExercise.class));
    }

    @Test
    void shouldThrowExceptionWhenWorkoutPlanDifferFromOneInWorkoutPlanExercise(){
        UUID workoutPlanExerciseId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        UUID anotherWorkoutPlanId = UUID.randomUUID();
        WorkoutPlan workoutPlan1 = new WorkoutPlan("Workout", "Workout", user);
        workoutPlan1.setId(anotherWorkoutPlanId);
        Exercise exercise = new Exercise("Test", "Test", "Test", "Test", user);
        exercise.setId(exerciseId);
        WorkoutPlanExercise workoutPlanExercise = new WorkoutPlanExercise(workoutPlan1, exercise, 1, 12, 90, 2);
        workoutPlanExercise.setId(workoutPlanExerciseId);
        when(workoutPlanExerciseRepository.findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, user.getId())).thenReturn(Optional.of(workoutPlanExercise));
        assertThrows(EntityNotFoundException.class, () -> workoutPlanExerciseService.updateWorkoutPlanExercise(workoutPlanId, workoutPlanExerciseId, new WorkoutPlanExerciseUpdateRequest(1, 2, 3, 4), user));
        verify(workoutPlanExerciseRepository).findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, user.getId());
        verify(workoutPlanExerciseRepository, never()).save(any(WorkoutPlanExercise.class));
    }

    @Test
    void shouldDeleteWorkoutPlanExercise(){
        UUID workoutPlanExerciseId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        Exercise exercise = new Exercise("Test", "Test", "Test", "Test", user);
        exercise.setId(exerciseId);
        WorkoutPlanExercise workoutPlanExercise = new WorkoutPlanExercise(workoutPlan, exercise, 1, 12, 90, 2);
        workoutPlanExercise.setId(workoutPlanExerciseId);
        when(workoutPlanExerciseRepository.findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, user.getId())).thenReturn(Optional.of(workoutPlanExercise));
        workoutPlanExerciseService.deleteWorkoutPlanExercise(workoutPlanId, workoutPlanExercise.getId(), user);
        verify(workoutPlanExerciseRepository).findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, user.getId());
        verify(workoutPlanExerciseRepository).delete(workoutPlanExercise);


    }

    @Test
    void shouldThrowExceptionWhenTryToDeleteWorkoutPlanExerciseDoesNotExist(){
        UUID workoutPlanExerciseId = UUID.randomUUID();
        when(workoutPlanExerciseRepository.findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, user.getId())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> workoutPlanExerciseService.deleteWorkoutPlanExercise(workoutPlanId, workoutPlanExerciseId, user));
        verify(workoutPlanExerciseRepository).findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, user.getId());
        verify(workoutPlanExerciseRepository, never()).delete(any(WorkoutPlanExercise.class));
    }

    @Test
    void shouldThrowExceptionWhenWorkoutPlanDoesNotMatch(){
        UUID workoutPlanExerciseId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        UUID anotherWorkoutPlanId = UUID.randomUUID();
        WorkoutPlan workoutPlan1 = new WorkoutPlan("Workout", "Workout", user);
        workoutPlan1.setId(anotherWorkoutPlanId);
        Exercise exercise = new Exercise("Test", "Test", "Test", "Test", user);
        exercise.setId(exerciseId);
        WorkoutPlanExercise workoutPlanExercise = new WorkoutPlanExercise(workoutPlan1, exercise, 1, 12, 90, 2);
        workoutPlanExercise.setId(workoutPlanExerciseId);
        when(workoutPlanExerciseRepository.findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, user.getId())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> workoutPlanExerciseService.deleteWorkoutPlanExercise(workoutPlanId, workoutPlanExerciseId, user));
        verify(workoutPlanExerciseRepository).findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, user.getId());
        verify(workoutPlanExerciseRepository, never()).delete(any(WorkoutPlanExercise.class));
    }

    @Test
    void shouldNotDeleteSomeoneElseWorkoutPlanExercise(){
        UUID workoutPlanExerciseId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        User otherUser = new User("otherUser", "otherUser", "otherUser", "otherUser");
        otherUser.setId(otherUserId);
        Exercise exercise = new Exercise("Test", "Test", "Test", "Test", user);
        exercise.setId(exerciseId);
        WorkoutPlanExercise workoutPlanExercise = new WorkoutPlanExercise(workoutPlan, exercise, 1, 12, 90, 2);
        workoutPlanExercise.setId(workoutPlanExerciseId);
        when(workoutPlanExerciseRepository.findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, otherUser.getId())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> workoutPlanExerciseService.deleteWorkoutPlanExercise(workoutPlanId, workoutPlanExerciseId, otherUser));
        verify(workoutPlanExerciseRepository).findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, otherUser.getId());
        verify(workoutPlanExerciseRepository, never()).delete(any(WorkoutPlanExercise.class));


    }
    @Test
    void shouldGetWorkoutPlanExercise(){
        UUID workoutPlanExerciseId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        Exercise exercise = new Exercise("Test", "Test", "Test", "Test", user);
        exercise.setId(exerciseId);
        WorkoutPlanExercise workoutPlanExercise = new WorkoutPlanExercise(workoutPlan, exercise, 1, 12, 90, 2);
        workoutPlanExercise.setId(workoutPlanExerciseId);
        when(workoutPlanExerciseRepository.findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, user.getId())).thenReturn(Optional.of(workoutPlanExercise));
        Optional<WorkoutPlanExerciseResponse> response = workoutPlanExerciseService.getWorkoutPlanExerciseById(workoutPlanExerciseId, workoutPlanId, user);
        assertTrue(response.isPresent());
        WorkoutPlanExerciseResponse workoutPlanExerciseResponse = response.get();
        assertAll(
                () -> assertEquals(workoutPlanExercise.getId(), workoutPlanExerciseResponse.id()),
                () -> assertEquals(workoutPlanExercise.getOrderIndex(), workoutPlanExerciseResponse.orderIndex()),
                () -> assertEquals(workoutPlanExercise.getRestSeconds(), workoutPlanExerciseResponse.restSeconds()),
                () -> assertEquals(workoutPlanExercise.getReps(), workoutPlanExerciseResponse.reps()),
                () -> assertEquals(workoutPlanExercise.getSets(), workoutPlanExerciseResponse.sets()),
                () -> assertEquals(workoutPlanExercise.getExercise().getId(), workoutPlanExerciseResponse.exerciseId())
        );
        verify(workoutPlanExerciseRepository).findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, user.getId());

    }

    @Test
    void shouldNotGetSomeoneElseWorkoutPlanExercise(){
        UUID workoutPlanExerciseId = UUID.randomUUID();
        User bob = new User("Bob", "Test", "test@example.com", "hashed-password");
        UUID bobId = UUID.randomUUID();
        bob.setId(bobId);
        when(workoutPlanExerciseRepository
                .findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(
                        workoutPlanExerciseId, workoutPlanId, bobId))
                .thenReturn(Optional.empty());
        assertTrue(workoutPlanExerciseService.getWorkoutPlanExerciseById(workoutPlanExerciseId, workoutPlanId, bob).isEmpty());
        verify(workoutPlanExerciseRepository).findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, bobId);

    }

    @Test
    void shouldNotGetNonExistentWorkoutPlanExercise() {

        UUID workoutPlanExerciseId = UUID.randomUUID();
        when(workoutPlanExerciseRepository.findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, user.getId())).thenReturn(Optional.empty());
        assertTrue(workoutPlanExerciseService.getWorkoutPlanExerciseById(workoutPlanExerciseId, workoutPlanId, user).isEmpty());
        verify(workoutPlanExerciseRepository).findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, user.getId());


    }

    @Test
    void shouldNotGetExerciseFromSomeoneElseWorkoutPlan() {
        User bob = new User("Bob", "user", "user@example.com", "hashed-password");
        bob.setId(UUID.randomUUID());
        when(workoutPlanRepository.existsByIdAndOwner_Id(workoutPlanId, bob.getId())).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> workoutPlanExerciseService.getExercisesByWorkoutPlanId(workoutPlanId, bob));
        verify(workoutPlanRepository).existsByIdAndOwner_Id(workoutPlanId, bob.getId());
        verifyNoInteractions(workoutPlanExerciseRepository);

    }
    @Test
    void shouldNotUpdateSomeoneElseWorkoutPlanExercise() {
        User bob = new User("Bob", "Test", "bob@example.com", "password");
        bob.setId(UUID.randomUUID());

        UUID exerciseId = UUID.randomUUID();
        UUID workoutPlanExerciseId = UUID.randomUUID();

        when(workoutPlanExerciseRepository
                .findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(
                        workoutPlanExerciseId, workoutPlanId, bob.getId()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> workoutPlanExerciseService.updateWorkoutPlanExercise(
                        workoutPlanId,
                        workoutPlanExerciseId,
                        new WorkoutPlanExerciseUpdateRequest(1, 2, 3, 4),
                        bob
                ));

        verify(workoutPlanExerciseRepository, never())
                .save(any(WorkoutPlanExercise.class));
    }
}

