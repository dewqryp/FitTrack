package org.dewqryp.fittrackbackend.orkoutsession.service;

import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workout.entity.WorkoutPlan;
import org.dewqryp.fittrackbackend.workout.repository.WorkoutPlanRepository;
import org.dewqryp.fittrackbackend.workoutplanexercises.entity.WorkoutPlanExercise;
import org.dewqryp.fittrackbackend.workoutsession.WorkoutSessionAlreadyInProgressException;
import org.dewqryp.fittrackbackend.workoutsession.dto.WorkoutSessionCreateRequest;
import org.dewqryp.fittrackbackend.workoutsession.dto.WorkoutSessionResponse;
import org.dewqryp.fittrackbackend.workoutsession.entity.WorkoutSession;
import org.dewqryp.fittrackbackend.workoutsession.repository.WorkoutSessionRepository;
import org.dewqryp.fittrackbackend.workoutsession.service.WorkoutSessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkoutSessionServiceTest {
    @Mock
    private WorkoutSessionRepository workoutSessionRepository;
    @Mock
    private WorkoutPlanRepository workoutPlanRepository;
    @InjectMocks
    private WorkoutSessionService workoutSessionService;

    @Test
    void shouldCreateWorkoutSession() {
        UUID workoutPlanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout", "Workout", user);
        workoutPlan.setId(workoutPlanId);
        WorkoutSessionCreateRequest createRequest = new WorkoutSessionCreateRequest(workoutPlanId, "Notes");
        when(workoutSessionRepository.existsByUser_IdAndFinishedAtIsNull(userId)).thenReturn(false);
        when(workoutPlanRepository.findByIdAndOwner_Id(workoutPlanId, userId)).thenReturn(Optional.of(workoutPlan));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        WorkoutSessionResponse response = workoutSessionService.startWorkoutSession(createRequest, user);
        assertAll(
                () -> assertEquals(workoutPlanId, response.workoutPlanId()),
                () -> assertNotNull(response.startedAt()),
                () -> assertEquals(createRequest.notes(), response.notes()),
                () -> assertNull(response.finishedAt())
        );
        verify(workoutSessionRepository).save(any(WorkoutSession.class));
        verify(workoutSessionRepository).existsByUser_IdAndFinishedAtIsNull(userId);
        verify(workoutPlanRepository).findByIdAndOwner_Id(workoutPlanId, userId);
    }

    @Test
    void shouldCreateWorkoutSessionWhenWorkoutPlanIsNull() {
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        WorkoutSessionCreateRequest createRequest = new WorkoutSessionCreateRequest(null, "Notes");
        when(workoutSessionRepository.existsByUser_IdAndFinishedAtIsNull(userId)).thenReturn(false);
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        WorkoutSessionResponse response = workoutSessionService.startWorkoutSession(createRequest, user);
        assertAll(
                () -> assertNull(response.workoutPlanId()),
                () -> assertNotNull(response.startedAt()),
                () -> assertEquals(createRequest.notes(), response.notes()),
                () -> assertNull(response.finishedAt())
        );
        verify(workoutSessionRepository).save(any(WorkoutSession.class));
        verify(workoutSessionRepository).existsByUser_IdAndFinishedAtIsNull(userId);
        verify(workoutPlanRepository, never())
                .findByIdAndOwner_Id(any(UUID.class), any(UUID.class));
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyHasActiveWorkoutSession(){
        UUID workoutPlanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        WorkoutSessionCreateRequest createRequest = new WorkoutSessionCreateRequest(workoutPlanId, "Notes");
        when(workoutSessionRepository.existsByUser_IdAndFinishedAtIsNull(userId)).thenReturn(true);
        assertThrows(WorkoutSessionAlreadyInProgressException.class, () -> workoutSessionService.startWorkoutSession(createRequest, user));
        verify(workoutSessionRepository, never()).save(any(WorkoutSession.class));
        verify(workoutSessionRepository).existsByUser_IdAndFinishedAtIsNull(userId);
        verify(workoutPlanRepository, never())
                .findByIdAndOwner_Id(workoutPlanId, userId);
    }

    @Test
    void shouldThrowExceptionWhenWorkoutPlanBelongsToAnotherUser(){
        UUID userId = UUID.randomUUID();
        UUID workoutPlanId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        WorkoutSessionCreateRequest createRequest = new WorkoutSessionCreateRequest(workoutPlanId, "Notes");
        when(workoutSessionRepository.existsByUser_IdAndFinishedAtIsNull(userId)).thenReturn(false);
        when(workoutPlanRepository.findByIdAndOwner_Id(workoutPlanId, userId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> workoutSessionService.startWorkoutSession(createRequest, user));
        verify(workoutSessionRepository, never()).save(any(WorkoutSession.class));
        verify(workoutSessionRepository).existsByUser_IdAndFinishedAtIsNull(userId);
        verify(workoutPlanRepository).findByIdAndOwner_Id(workoutPlanId, userId);

    }
}
