package org.dewqryp.fittrackbackend.workoutplan.service;

import org.dewqryp.fittrackbackend.exercise.entity.Exercise;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workout.dto.WorkoutPlanResponse;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

}
