package org.dewqryp.fittrackbackend.exerciseset.service;

import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.exercise.entity.Exercise;
import org.dewqryp.fittrackbackend.exercise.repository.ExerciseRepository;
import org.dewqryp.fittrackbackend.exerciseset.dto.ExerciseSetCreateRequest;
import org.dewqryp.fittrackbackend.exerciseset.dto.ExerciseSetResponse;
import org.dewqryp.fittrackbackend.exerciseset.entity.ExerciseSet;
import org.dewqryp.fittrackbackend.exerciseset.exception.ExerciseSetAlreadyExistsException;
import org.dewqryp.fittrackbackend.exerciseset.repository.ExerciseSetRepository;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workout.entity.WorkoutPlan;
import org.dewqryp.fittrackbackend.workoutsession.entity.WorkoutSession;
import org.dewqryp.fittrackbackend.workoutsession.exceptions.WorkoutSessionIsAlreadyFinishedException;
import org.dewqryp.fittrackbackend.workoutsession.repository.WorkoutSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExerciseSetServiceTest {
    @Mock
    private ExerciseSetRepository exerciseSetRepository;
    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private WorkoutSessionRepository workoutSessionRepository;
    @InjectMocks
    private ExerciseSetService exerciseSetService;

    @Test
    void shouldCreateExercisesSet() {
        UUID userId = UUID.randomUUID();
        UUID workoutSessionId = UUID.randomUUID();
        UUID workoutPlanId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout", "Workout", user);
        workoutPlan.setId(workoutPlanId);
        WorkoutSession workoutSession = new WorkoutSession(user, workoutPlan, "Notes");
        workoutSession.setId(workoutSessionId);
        Exercise exercise = new Exercise("Exercise", "Exercise", "Exercise", "Exercise", user);
        exercise.setId(exerciseId);
        ExerciseSetCreateRequest createRequest = new ExerciseSetCreateRequest(exerciseId, 1, 6, BigDecimal.TWO);
        when(workoutSessionRepository.findByIdAndUser_Id(workoutSessionId, userId)).thenReturn(Optional.of(workoutSession));
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));
        when(exerciseSetRepository.existsByWorkoutSession_IdAndExercise_IdAndSetNumber(workoutSessionId, exerciseId, createRequest.setNumber())).thenReturn(false);
        when(exerciseSetRepository.save(any(ExerciseSet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ExerciseSetResponse response = exerciseSetService.createExerciseSet(workoutSessionId, createRequest, user);
        assertAll(
                () -> assertEquals(workoutSessionId, response.workoutSessionId()),
                () -> assertEquals(exerciseId, response.exerciseId()),
                () -> assertEquals(createRequest.reps(), response.reps()),
                () -> assertEquals(createRequest.setNumber(), response.setNumber()),
                () -> assertEquals(createRequest.weight(), response.weight())

        );
        verify(workoutSessionRepository).findByIdAndUser_Id(workoutSessionId, userId);
        verify(exerciseRepository).findById(exerciseId);
        verify(exerciseSetRepository).existsByWorkoutSession_IdAndExercise_IdAndSetNumber(workoutSessionId, exerciseId, createRequest.setNumber());

    }
    @Test
    void shouldThrowExceptionWhenWorkoutSessionNotFound() {
        UUID sessionId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "password");

        ExerciseSetCreateRequest request = new ExerciseSetCreateRequest(
                UUID.randomUUID(), 1, 6, BigDecimal.TWO
        );

        when(workoutSessionRepository.findByIdAndUser_Id(sessionId, user.getId()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> exerciseSetService.createExerciseSet(sessionId, request, user));

        verifyNoInteractions(exerciseRepository, exerciseSetRepository);
    }
    @Test
    void shouldThrowExceptionWhenWorkoutSessionBelongsToAnotherUser() {
        UUID currentUserId = UUID.randomUUID();
        UUID sessionOwnerId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        User currentUser = new User("User", "User", "user@example.com", "password");
        currentUser.setId(currentUserId);

        ExerciseSetCreateRequest request = new ExerciseSetCreateRequest(
                UUID.randomUUID(), 1, 6, BigDecimal.TWO
        );

        when(workoutSessionRepository.findByIdAndUser_Id(sessionId, currentUserId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> exerciseSetService.createExerciseSet(sessionId, request, currentUser));

        verify(workoutSessionRepository).findByIdAndUser_Id(sessionId, currentUserId);
        verifyNoInteractions(exerciseRepository, exerciseSetRepository);

        assertNotEquals(currentUserId, sessionOwnerId);
    }
    @Test
    void shouldThrowExceptionWhenWorkoutSessionIsAlreadyFinished() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        User user = new User("User", "User", "user@example.com", "password");
        user.setId(userId);

        WorkoutSession session = new WorkoutSession(user, null, "Notes");
        session.setId(sessionId);
        session.setFinishedAt(OffsetDateTime.now());

        ExerciseSetCreateRequest request = new ExerciseSetCreateRequest(
                UUID.randomUUID(), 1, 6, BigDecimal.TWO
        );

        when(workoutSessionRepository.findByIdAndUser_Id(sessionId, userId))
                .thenReturn(Optional.of(session));

        assertThrows(WorkoutSessionIsAlreadyFinishedException.class,
                () -> exerciseSetService.createExerciseSet(sessionId, request, user));

        verifyNoInteractions(exerciseRepository, exerciseSetRepository);
    }
    @Test
    void shouldThrowExceptionWhenExerciseSetAlreadyExists() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();

        User user = new User("User", "User", "user@example.com", "password");
        user.setId(userId);

        WorkoutSession session = new WorkoutSession(user, null, "Notes");
        session.setId(sessionId);

        Exercise exercise = new Exercise(
                "Exercise", "Description", "Muscle", "Equipment", user
        );
        exercise.setId(exerciseId);

        ExerciseSetCreateRequest request = new ExerciseSetCreateRequest(
                exerciseId, 1, 6, BigDecimal.TWO
        );

        when(workoutSessionRepository.findByIdAndUser_Id(sessionId, userId))
                .thenReturn(Optional.of(session));

        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.of(exercise));

        when(exerciseSetRepository.existsByWorkoutSession_IdAndExercise_IdAndSetNumber(
                sessionId, exerciseId, request.setNumber()
        )).thenReturn(true);

        assertThrows(ExerciseSetAlreadyExistsException.class,
                () -> exerciseSetService.createExerciseSet(sessionId, request, user));

        verify(exerciseSetRepository, never()).save(any(ExerciseSet.class));
    }


    @Test
    void shouldReturnAllExerciseSets(){
        UUID userId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashedPassword");
        user.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout plan", "Workout plan", user);
        workoutPlan.setId(UUID.randomUUID());
        WorkoutSession session = new WorkoutSession(user, workoutPlan, "Notes");
        session.setId(sessionId);
        Exercise exercise = new Exercise("Bemch press", "Bench press", "Chest", "Barbell", user);
        exercise.setId(exerciseId);
        ExerciseSet exerciseSet1 = new ExerciseSet(session, exercise, 1, 8, BigDecimal.valueOf(82.5));
        exerciseSet1.setId(UUID.randomUUID());
        ExerciseSet exerciseSet2 = new ExerciseSet(session, exercise, 2, 8, BigDecimal.valueOf(82.5));
        exerciseSet2.setId(UUID.randomUUID());
        ExerciseSet exerciseSet3 = new ExerciseSet(session, exercise, 3, 8, BigDecimal.valueOf(82.5));
        exerciseSet3.setId(UUID.randomUUID());
        when(workoutSessionRepository.findByIdAndUser_Id(sessionId, userId)).thenReturn(Optional.of(session));
        when(exerciseSetRepository.findAllByWorkoutSession_IdOrderBySetNumberAsc(sessionId)).thenReturn(List.of(exerciseSet1, exerciseSet2, exerciseSet3));
        List<ExerciseSetResponse> responses = exerciseSetService.getExerciseSets(sessionId, user);
        assertAll(
                () -> assertEquals(3, responses.size()),
                () -> assertEquals(exerciseSet1.getWeight().doubleValue(), responses.getFirst().weight().doubleValue()),
                () -> assertEquals(exerciseSet1.getSetNumber(), responses.getFirst().setNumber()),
                () -> assertEquals(exerciseSet1.getReps(), responses.getFirst().reps()),
                () -> assertEquals(exerciseSet1.getExercise().getId(), responses.getFirst().exerciseId()),
                () -> assertEquals(exerciseSet1.getWorkoutSession().getId(), responses.getFirst().workoutSessionId()),
                () -> assertEquals(2, responses.get(1).setNumber()),
                () -> assertEquals(3, responses.get(2).setNumber())
        );
        verify(workoutSessionRepository).findByIdAndUser_Id(sessionId, userId);
        verify(exerciseSetRepository).findAllByWorkoutSession_IdOrderBySetNumberAsc(sessionId);

    }

    @Test
    void shouldReturnEmptyListWhenNoExerciseSets(){
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashedPassword");
        user.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout plan", "Workout plan", user);
        workoutPlan.setId(UUID.randomUUID());
        WorkoutSession session = new WorkoutSession(user, workoutPlan, "Notes");
        session.setId(sessionId);
        when(workoutSessionRepository.findByIdAndUser_Id(sessionId, userId)).thenReturn(Optional.of(session));
        when(exerciseSetRepository.findAllByWorkoutSession_IdOrderBySetNumberAsc(sessionId)).thenReturn(Collections.emptyList());
        List<ExerciseSetResponse> responses = exerciseSetService.getExerciseSets(sessionId, user);
        assertTrue(responses.isEmpty());
        verify(workoutSessionRepository).findByIdAndUser_Id(sessionId, userId);
        verify(exerciseSetRepository).findAllByWorkoutSession_IdOrderBySetNumberAsc(sessionId);

    }

    @Test
    void shouldThrowExceptionWhenWorkoutSessionIsNotFound(){
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashedPassword");
        user.setId(userId);
        when(workoutSessionRepository.findByIdAndUser_Id(sessionId, userId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> exerciseSetService.getExerciseSets(sessionId, user));
        verify(workoutSessionRepository).findByIdAndUser_Id(sessionId, userId);
        verify(exerciseSetRepository, never()).findAllByWorkoutSession_IdOrderBySetNumberAsc(sessionId);

    }

    @Test
    void shouldReturnExerciseSetsWhenWorkoutSessionIsFinished(){
        UUID userId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashedPassword");
        user.setId(userId);
        WorkoutPlan workoutPlan = new WorkoutPlan("Workout plan", "Workout plan", user);
        workoutPlan.setId(UUID.randomUUID());
        WorkoutSession session = new WorkoutSession(user, workoutPlan, "Notes");
        session.setId(sessionId);
        session.setFinishedAt(OffsetDateTime.now().minusDays(1));
        Exercise exercise = new Exercise("Bemch press", "Bench press", "Chest", "Barbell", user);
        exercise.setId(exerciseId);
        ExerciseSet exerciseSet1 = new ExerciseSet(session, exercise, 1, 8, BigDecimal.valueOf(82.5));
        exerciseSet1.setId(UUID.randomUUID());
        ExerciseSet exerciseSet2 = new ExerciseSet(session, exercise, 2, 8, BigDecimal.valueOf(82.5));
        exerciseSet2.setId(UUID.randomUUID());
        ExerciseSet exerciseSet3 = new ExerciseSet(session, exercise, 3, 8, BigDecimal.valueOf(82.5));
        exerciseSet3.setId(UUID.randomUUID());
        when(workoutSessionRepository.findByIdAndUser_Id(sessionId, userId)).thenReturn(Optional.of(session));
        when(exerciseSetRepository.findAllByWorkoutSession_IdOrderBySetNumberAsc(sessionId)).thenReturn(List.of(exerciseSet1, exerciseSet2, exerciseSet3));
        List<ExerciseSetResponse> responses = exerciseSetService.getExerciseSets(sessionId, user);
        assertAll(
                () -> assertEquals(3, responses.size()),
                () -> assertEquals(exerciseSet1.getWeight().doubleValue(), responses.getFirst().weight().doubleValue()),
                () -> assertEquals(exerciseSet1.getSetNumber(), responses.getFirst().setNumber()),
                () -> assertEquals(exerciseSet1.getReps(), responses.getFirst().reps()),
                () -> assertEquals(exerciseSet1.getExercise().getId(), responses.getFirst().exerciseId()),
                () -> assertEquals(exerciseSet1.getWorkoutSession().getId(), responses.getFirst().workoutSessionId()),
                () -> assertEquals(2, responses.get(1).setNumber()),
                () -> assertEquals(3, responses.get(2).setNumber())
        );
        verify(workoutSessionRepository).findByIdAndUser_Id(sessionId, userId);
        verify(exerciseSetRepository).findAllByWorkoutSession_IdOrderBySetNumberAsc(sessionId);
    }



}
