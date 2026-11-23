package org.dewqryp.fittrackbackend.workoutplan.service;

import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.exercise.entity.Exercise;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workout.dto.WorkoutPlanResponse;
import org.dewqryp.fittrackbackend.workout.dto.WorkoutPlanUpdateRequest;
import org.dewqryp.fittrackbackend.workout.entity.WorkoutPlan;
import org.dewqryp.fittrackbackend.workout.repository.WorkoutPlanRepository;
import org.dewqryp.fittrackbackend.workout.service.WorkoutPlanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkoutPlanServiceTest {
    @Mock
    private WorkoutPlanRepository workoutPlanRepository;

    @InjectMocks
    private WorkoutPlanService workoutPlanService;

    @Test
    void shouldReturnWorkoutPlanWhenOwnerMatches(){
        UUID userId = UUID.randomUUID();
        UUID workoutPlanId = UUID.randomUUID();
        User alice = new User("Alice", "Yar", "alice@example.com", "hashed-password");
        alice.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout", "Test", alice);
        workoutPlan.setId(workoutPlanId);
        when(workoutPlanRepository.findByIdAndOwner_Id(workoutPlanId, userId)).thenReturn(Optional.of(workoutPlan));
        WorkoutPlanResponse workoutPlanResponse = workoutPlanService.findById(workoutPlanId, alice).orElseThrow();
        assertEquals(workoutPlan.getId(), workoutPlanResponse.id());
        assertEquals(workoutPlan.getDescription(), workoutPlanResponse.description());
        assertEquals(workoutPlan.getCreatedAt(), workoutPlanResponse.createdAt());
        assertEquals(workoutPlan.getName(), workoutPlanResponse.name());
        assertEquals(workoutPlan.getOwner().getId(), workoutPlanResponse.ownerId());
        verify(workoutPlanRepository).findByIdAndOwner_Id(workoutPlanId, userId);

    }

    @Test
    void shouldReturnEmptyWhenOwnerDoesNotMatch(){
        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();
        UUID workoutPlanId = UUID.randomUUID();
        User alice = new User("Alice", "Yar", "alice@example.com", "hashed-password");
        User bob = new User("Bob", "Bob", "bob@example.com", "hashed-password");
        alice.setId(aliceId);
        bob.setId(bobId);
        when(workoutPlanRepository.findByIdAndOwner_Id(workoutPlanId, bobId)).thenReturn(Optional.empty());
        Optional<WorkoutPlanResponse> response = workoutPlanService.findById(workoutPlanId, bob);
        assertTrue(response.isEmpty());
        verify(workoutPlanRepository).findByIdAndOwner_Id(workoutPlanId, bobId);

    }

    @Test
    void shouldReturnEmptyWhenWorkoutPlanDoesNotExist(){
        UUID workoutPlanId = UUID.randomUUID();
        UUID aliceId = UUID.randomUUID();
        User alice = new User("Alice", "Yar", "alice@example.com", "hashed-password");
        alice.setId(aliceId);
        when(workoutPlanRepository.findByIdAndOwner_Id(workoutPlanId, aliceId)).thenReturn(Optional.empty());
        Optional<WorkoutPlanResponse> response = workoutPlanService.findById(workoutPlanId, alice);
        assertTrue(response.isEmpty());
        verify(workoutPlanRepository).findByIdAndOwner_Id(workoutPlanId, aliceId);
    }

    @Test
    void shouldDeleteWorkoutPlan(){
        UUID workoutPlanId = UUID.randomUUID();
        UUID aliceId = UUID.randomUUID();
        User alice = new User("Alice", "Yar", "alice@example.com", "hashed-password");
        alice.setId(aliceId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout", "Test", alice);
        workoutPlan.setId(workoutPlanId);
        when(workoutPlanRepository.findByIdAndOwner_Id(workoutPlanId, aliceId)).thenReturn(Optional.of(workoutPlan));
        workoutPlanService.deleteWorkoutPlan(workoutPlanId, alice);
        verify(workoutPlanRepository).findByIdAndOwner_Id(workoutPlanId, aliceId);
        verify(workoutPlanRepository).delete(workoutPlan);
    }

    @Test
    void shouldNotDeleteSomeoneElseWorkoutPlan(){
        UUID workoutPlanId = UUID.randomUUID();
        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();
        User alice = new User("Alice", "Yar", "alice@example.com", "hashed-password");
        User bob = new User("Bob", "Bob", "bob@example.com", "hashed-password");
        alice.setId(aliceId);
        bob.setId(bobId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout", "Test", alice);
        workoutPlan.setId(workoutPlanId);
        when(workoutPlanRepository.findByIdAndOwner_Id(workoutPlanId, bobId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> workoutPlanService.deleteWorkoutPlan(workoutPlanId, bob));
        verify(workoutPlanRepository).findByIdAndOwner_Id(workoutPlanId, bobId);
        verify(workoutPlanRepository, never()).delete(workoutPlan);
    }

    @Test
    void shouldNotDeleteNonExistentWorkoutPlan(){
        UUID workoutPlanId = UUID.randomUUID();
        UUID aliceId = UUID.randomUUID();
        User alice = new User("Alice", "Yar", "alice@example.com", "hashed-password");
        alice.setId(aliceId);
        when(workoutPlanRepository.findByIdAndOwner_Id(workoutPlanId, aliceId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> workoutPlanService.deleteWorkoutPlan(workoutPlanId, alice));
        verify(workoutPlanRepository).findByIdAndOwner_Id(workoutPlanId, aliceId);
        verify(workoutPlanRepository, never()).delete(any(WorkoutPlan.class));
    }

    @Test
    void shouldUpdateWorkoutPlan(){
        UUID workoutPlanId = UUID.randomUUID();
        UUID aliceId = UUID.randomUUID();
        User alice = new User("Alice", "Yar", "alice@example.com", "hashed-password");
        alice.setId(aliceId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout", "Test", alice);
        workoutPlan.setId(workoutPlanId);
        WorkoutPlanUpdateRequest request = new WorkoutPlanUpdateRequest("Update", "Update");
        when(workoutPlanRepository.save(workoutPlan)).thenReturn(workoutPlan);
        when(workoutPlanRepository.findByIdAndOwner_Id(workoutPlanId, aliceId)).thenReturn(Optional.of(workoutPlan));
        WorkoutPlanResponse response = workoutPlanService.updateWorkoutPlan(workoutPlanId, request, alice);
        assertAll(
                () -> assertEquals(workoutPlan.getId(), response.id()),
                () -> assertEquals(workoutPlan.getOwner().getId(), response.ownerId()),
                () -> assertEquals(request.name(), response.name()),
                () -> assertEquals(request.description(), response.description()),
                () -> assertEquals(workoutPlan.getCreatedAt(), response.createdAt())
        );
        verify(workoutPlanRepository).findByIdAndOwner_Id(workoutPlanId, aliceId);
        verify(workoutPlanRepository).save(workoutPlan);
    }

    @Test
    void shouldNotUpdateSomeoneElseWorkoutPlan(){
        UUID workoutPlanId = UUID.randomUUID();
        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();
        User alice = new User("Alice", "Yar", "alice@example.com", "hashed-password");
        User bob = new User("Bob", "Bob", "bob@example.com", "hashed-password");
        alice.setId(aliceId);
        bob.setId(bobId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout", "Test", bob);
        workoutPlan.setId(workoutPlanId);
        WorkoutPlanUpdateRequest request = new WorkoutPlanUpdateRequest("Update", "Update");
        when(workoutPlanRepository.findByIdAndOwner_Id(workoutPlanId, aliceId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> workoutPlanService.updateWorkoutPlan(workoutPlanId, request, alice));
        verify(workoutPlanRepository).findByIdAndOwner_Id(workoutPlanId, aliceId);
        verify(workoutPlanRepository, never()).save(workoutPlan);
    }

    @Test
    void shouldNotUpdateNonExistentWorkoutPlan(){
        UUID workoutPlanId = UUID.randomUUID();
        UUID aliceId = UUID.randomUUID();
        User alice = new User("Alice", "Yar", "alice@example.com", "hashed-password");
        alice.setId(aliceId);
        WorkoutPlanUpdateRequest request = new WorkoutPlanUpdateRequest("Update", "Update");
        when(workoutPlanRepository.findByIdAndOwner_Id(workoutPlanId, aliceId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> workoutPlanService.updateWorkoutPlan(workoutPlanId, request, alice));
        verify(workoutPlanRepository).findByIdAndOwner_Id(workoutPlanId, aliceId);
        verify(workoutPlanRepository, never()).save(any(WorkoutPlan.class));
    }
}
