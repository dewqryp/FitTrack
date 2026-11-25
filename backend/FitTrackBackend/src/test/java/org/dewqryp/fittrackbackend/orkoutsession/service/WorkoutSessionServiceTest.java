package org.dewqryp.fittrackbackend.orkoutsession.service;

import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workout.entity.WorkoutPlan;
import org.dewqryp.fittrackbackend.workout.repository.WorkoutPlanRepository;
import org.dewqryp.fittrackbackend.workoutsession.dto.WorkoutSessionUpdateRequest;
import org.dewqryp.fittrackbackend.workoutsession.exceptions.WorkoutSessionAlreadyInProgressException;
import org.dewqryp.fittrackbackend.workoutsession.dto.WorkoutSessionCreateRequest;
import org.dewqryp.fittrackbackend.workoutsession.dto.WorkoutSessionResponse;
import org.dewqryp.fittrackbackend.workoutsession.entity.WorkoutSession;
import org.dewqryp.fittrackbackend.workoutsession.exceptions.WorkoutSessionIsAlreadyFinishedException;
import org.dewqryp.fittrackbackend.workoutsession.repository.WorkoutSessionRepository;
import org.dewqryp.fittrackbackend.workoutsession.service.WorkoutSessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.*;

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

    @Test
    void shouldFinishWorkoutSession() {
        UUID workoutPlanId = UUID.randomUUID();
        UUID workoutSessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout", "Workout", user);
        workoutPlan.setId(workoutPlanId);
        WorkoutSession workoutSession = new WorkoutSession(user, workoutPlan, "Notes");
        workoutSession.setId(workoutSessionId);
        when(workoutSessionRepository.findByIdAndUser_Id(workoutSessionId, userId)).thenReturn(Optional.of(workoutSession));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        WorkoutSessionResponse response = workoutSessionService.finishWorkoutSession(workoutSessionId, user);
        assertAll(
                () -> assertEquals(workoutPlanId, response.workoutPlanId()),
                () -> assertEquals(workoutSession.getNotes(), response.notes()),
                () -> assertTrue(response.finishedAt().isBefore(OffsetDateTime.now()))
        );
        verify(workoutSessionRepository).save(any(WorkoutSession.class));
        verify(workoutSessionRepository).save(argThat(
                session -> session.getFinishedAt() != null
        ));
    }

    @Test
    void shouldThrowWhenSessionAlreadyFinished(){
        UUID workoutPlanId = UUID.randomUUID();
        UUID workoutSessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout", "Workout", user);
        workoutPlan.setId(workoutPlanId);
        WorkoutSession workoutSession = new WorkoutSession(user, workoutPlan, "Notes");
        workoutSession.setId(workoutSessionId);
        workoutSession.setFinishedAt(OffsetDateTime.now());
        when(workoutSessionRepository.findByIdAndUser_Id(workoutSessionId, userId)).thenReturn(Optional.of(workoutSession));
        assertThrows(WorkoutSessionIsAlreadyFinishedException.class, () -> workoutSessionService.finishWorkoutSession(workoutSessionId, user));
        verify(workoutSessionRepository, never()).save(any(WorkoutSession.class));
        verify(workoutSessionRepository).findByIdAndUser_Id(workoutSessionId, userId);


    }

    @Test
    void shouldThrowWhenSessionDoesNotExist(){
        UUID workoutSessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        when(workoutSessionRepository.findByIdAndUser_Id(workoutSessionId, userId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> workoutSessionService.finishWorkoutSession(workoutSessionId, user));
        verify(workoutSessionRepository, never()).save(any(WorkoutSession.class));
        verify(workoutSessionRepository).findByIdAndUser_Id(workoutSessionId, userId);
    }

    @Test
    void shouldUpdateWorkoutSessionNotes(){
        UUID workoutSessionId = UUID.randomUUID();
        UUID workoutPlanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout", "Workout", user);
        workoutPlan.setId(workoutPlanId);
        WorkoutSessionUpdateRequest updateRequest = new WorkoutSessionUpdateRequest("Updated");
        WorkoutSession workoutSession = new WorkoutSession(user, workoutPlan, "Notes");
        workoutSession.setId(workoutSessionId);
        when(workoutSessionRepository.findByIdAndUser_Id(workoutSessionId, userId)).thenReturn(Optional.of(workoutSession));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        WorkoutSessionResponse response = workoutSessionService.updateWorkoutSession(workoutSessionId, updateRequest, user);
        assertAll(
                () -> assertEquals(workoutSession.getWorkoutPlan().getId(), response.workoutPlanId()),
                () -> assertEquals(workoutSession.getStartedAt(), response.startedAt()),
                () -> assertEquals(updateRequest.notes(), response.notes())
        );
        verify(workoutSessionRepository).findByIdAndUser_Id(workoutSessionId, userId);
        verify(workoutSessionRepository).save(any(WorkoutSession.class));

    }

    @Test
    void shouldThrowWhenUpdatingFinishedSession(){
        UUID workoutSessionId = UUID.randomUUID();
        UUID workoutPlanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout", "Workout", user);
        workoutPlan.setId(workoutPlanId);
        WorkoutSessionUpdateRequest updateRequest = new WorkoutSessionUpdateRequest("Updated");
        WorkoutSession workoutSession = new WorkoutSession(user, workoutPlan, "Notes");
        workoutSession.setFinishedAt(OffsetDateTime.now());
        when(workoutSessionRepository.findByIdAndUser_Id(workoutSessionId, userId)).thenReturn(Optional.of(workoutSession));
        assertThrows(WorkoutSessionIsAlreadyFinishedException.class, () -> workoutSessionService.updateWorkoutSession(workoutSessionId, updateRequest, user));
        verify(workoutSessionRepository).findByIdAndUser_Id(workoutSessionId, userId);
        verify(workoutSessionRepository, never()).save(any(WorkoutSession.class));
    }

    @Test
    void shouldThrowWhenUpdatingNonexistentSession(){
        UUID workoutSessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        WorkoutSessionUpdateRequest updateRequest = new WorkoutSessionUpdateRequest("Updated");
        when(workoutSessionRepository.findByIdAndUser_Id(workoutSessionId, userId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> workoutSessionService.updateWorkoutSession(workoutSessionId, updateRequest, user));
        verify(workoutSessionRepository).findByIdAndUser_Id(workoutSessionId, userId);
        verify(workoutSessionRepository, never()).save(any(WorkoutSession.class));

    }

    @Test
    void shouldReturnAllWorkoutSessions(){
        UUID workoutPlanId = UUID.randomUUID();
        UUID workoutSessionId1 = UUID.randomUUID();
        UUID workoutSessionId2 = UUID.randomUUID();
        UUID workoutSessionId3 = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout", "Workout", user);
        workoutPlan.setId(workoutPlanId);
        WorkoutSession workoutSession1 = new WorkoutSession(user, workoutPlan, "1");
        workoutSession1.setId(workoutSessionId1);
        WorkoutSession workoutSession2 = new WorkoutSession(user, workoutPlan, "2");
        workoutSession2.setId(workoutSessionId2);
        WorkoutSession workoutSession3 = new WorkoutSession(user, workoutPlan, "3");
        workoutSession3.setId(workoutSessionId3);
        when(workoutSessionRepository.findAllByUser_IdOrderByStartedAtDesc(userId)).thenReturn(Arrays.asList(workoutSession1, workoutSession2, workoutSession3));
        List<WorkoutSessionResponse> responseList = workoutSessionService.getAllWorkoutSessions(user);
        assertAll(
                () -> assertEquals(3, responseList.size()),
                () -> assertEquals(workoutSession1.getNotes(), responseList.getFirst().notes()),
                () -> assertEquals(workoutSession1.getStartedAt(), responseList.getFirst().startedAt()),
                () -> assertEquals(workoutSession1.getStartedAt(), responseList.getFirst().startedAt())
        );
        verify(workoutSessionRepository).findAllByUser_IdOrderByStartedAtDesc(userId);


    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoSessions(){
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        when(workoutSessionRepository.findAllByUser_IdOrderByStartedAtDesc(userId)).thenReturn(Collections.emptyList());
        List<WorkoutSessionResponse> responseList = workoutSessionService.getAllWorkoutSessions(user);
        assertTrue(responseList.isEmpty());
        verify(workoutSessionRepository).findAllByUser_IdOrderByStartedAtDesc(userId);

    }

    @Test
    void shouldReturnSessionsOrderedByStartedAtDesc(){
        UUID workoutPlanId = UUID.randomUUID();
        UUID workoutSessionId1 = UUID.randomUUID();
        UUID workoutSessionId2 = UUID.randomUUID();
        UUID workoutSessionId3 = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout", "Workout", user);
        workoutPlan.setId(workoutPlanId);
        WorkoutSession workoutSession1 = new WorkoutSession(user, workoutPlan, "1");
        workoutSession1.setStartedAt(OffsetDateTime.now().minusDays(3));
        workoutSession1.setId(workoutSessionId1);
        WorkoutSession workoutSession2 = new WorkoutSession(user, workoutPlan, "2");
        workoutSession2.setStartedAt(OffsetDateTime.now().minusDays(2));
        workoutSession2.setId(workoutSessionId2);
        WorkoutSession workoutSession3 = new WorkoutSession(user, workoutPlan, "3");
        workoutSession3.setStartedAt(OffsetDateTime.now().minusDays(1));
        workoutSession3.setId(workoutSessionId3);
        when(workoutSessionRepository.findAllByUser_IdOrderByStartedAtDesc(userId)).thenReturn(Arrays.asList(workoutSession3, workoutSession2, workoutSession1));
        List<WorkoutSessionResponse> responseList = workoutSessionService.getAllWorkoutSessions(user);
        assertTrue(responseList.getFirst().startedAt().isAfter(responseList.get(1).startedAt()));
        assertTrue(responseList.getFirst().startedAt().isAfter(responseList.get(2).startedAt()));
        verify(workoutSessionRepository).findAllByUser_IdOrderByStartedAtDesc(userId);
    }

    @Test
    void shouldReturnWorkoutSessionById(){
        UUID workoutSessionId = UUID.randomUUID();
        UUID workoutPlanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout", "Workout", user);
        workoutPlan.setId(workoutPlanId);
        WorkoutSession workoutSession  = new WorkoutSession(user, workoutPlan, "notes");
        workoutSession.setStartedAt(OffsetDateTime.now().minusDays(3));
        workoutSession.setId(workoutSessionId);
        when(workoutSessionRepository.findByIdAndUser_Id(workoutSessionId, userId)).thenReturn(Optional.of(workoutSession));
        WorkoutSessionResponse response = workoutSessionService.getWorkoutSessionById(workoutSessionId, user).get();
        assertAll(
                () -> assertEquals(workoutSession.getStartedAt(), response.startedAt()),
                () -> assertEquals(workoutSession.getFinishedAt(), response.finishedAt()),
                () -> assertEquals(workoutSession.getNotes(), response.notes()),
                () -> assertEquals(workoutSession.getWorkoutPlan().getId(), response.workoutPlanId()),
                () -> assertEquals(workoutSessionId, response.id())
        );
        verify(workoutSessionRepository).findByIdAndUser_Id(workoutSessionId, userId);


    }

    @Test
    void shouldReturnEmptyWhenWorkoutSessionDoesNotExist(){
        UUID workoutSessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        when(workoutSessionRepository.findByIdAndUser_Id(workoutSessionId, userId)).thenReturn(Optional.empty());
        assertTrue(workoutSessionService.getWorkoutSessionById(workoutSessionId, user).isEmpty());
        verify(workoutSessionRepository).findByIdAndUser_Id(workoutSessionId, userId);
    }

    @Test
    void shouldReturnEmptyWhenWorkoutSessionBelongsToAnotherUser(){
        UUID workoutSessionId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID workoutPlanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        User currentUser = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        currentUser.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout", "Workout", user);
        workoutPlan.setId(workoutPlanId);
        WorkoutSession workoutSession  = new WorkoutSession(user, workoutPlan, "notes");
        workoutSession.setStartedAt(OffsetDateTime.now().minusDays(3));
        workoutSession.setId(workoutSessionId);
        when(workoutSessionRepository.findByIdAndUser_Id(workoutSessionId, currentUserId)).thenReturn(Optional.empty());
        assertTrue(workoutSessionService.getWorkoutSessionById(workoutSessionId, currentUser).isEmpty());
        verify(workoutSessionRepository).findByIdAndUser_Id(workoutSessionId, currentUserId);

    }

}
