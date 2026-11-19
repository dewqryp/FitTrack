package org.dewqryp.fittrackbackend.exercise.service;

import org.dewqryp.fittrackbackend.exercise.dto.ExerciseCreateRequest;
import org.dewqryp.fittrackbackend.exercise.dto.ExerciseResponse;
import org.dewqryp.fittrackbackend.exercise.entity.Exercise;
import org.dewqryp.fittrackbackend.exercise.repository.ExerciseRepository;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExerciseServiceTest {
    @Mock
    private ExerciseRepository exerciseRepository;
    @InjectMocks
    private ExerciseService exerciseService;

    @Test
    void shouldCreateExercise(){
        UUID exerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ExerciseCreateRequest createRequest = new ExerciseCreateRequest("Test", "Test", "Test", "Test");
        User currentUser = new User("Test", "Test", "Test", "hashed-password");
        currentUser.setId(userId);
        Exercise exercise = new Exercise("Test", "Test", "Test", "Test", currentUser);
        exercise.setId(exerciseId);
        when(exerciseRepository.save(any(Exercise.class))).thenReturn(exercise);
        ExerciseResponse response = exerciseService.create(createRequest,currentUser);
        assertAll(() -> {
            assertEquals(exercise.getId(), response.id());
            assertEquals(exercise.getName(), response.name());
            assertEquals(exercise.getDescription(), response.description());
            assertEquals(exercise.getEquipment(), response.equipment());
            assertEquals(exercise.getCreatedBy().getId(), response.createdBy());
            assertEquals(exercise.getMuscleGroup(), response.muscleGroup());
        });
        ArgumentCaptor<Exercise> exerciseCaptor =
                ArgumentCaptor.forClass(Exercise.class);

        verify(exerciseRepository).save(exerciseCaptor.capture());

        Exercise savedExercise = exerciseCaptor.getValue();

        assertAll(
                () -> assertEquals(createRequest.name(), savedExercise.getName()),
                () -> assertEquals(createRequest.description(), savedExercise.getDescription()),
                () -> assertEquals(createRequest.muscleGroup(), savedExercise.getMuscleGroup()),
                () -> assertEquals(createRequest.equipment(), savedExercise.getEquipment()),
                () -> assertSame(currentUser, savedExercise.getCreatedBy())
        );
    }

    @Test
    void shouldFindAllExercisesInRepositoryOrder(){
        UUID exerciseId1 = UUID.randomUUID();
        UUID exerciseId2 = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User currentUser = new User("Test", "Test", "Test", "hashed-password");
        currentUser.setId(userId);
        Exercise exercise1 = new Exercise("Test1", "Test1", "Test1", "Test1", currentUser);
        Exercise exercise2 = new Exercise("Test2", "Test2", "Test2", "Test2",currentUser);
        exercise1.setId(exerciseId1);
        exercise2.setId(exerciseId2);
        when(exerciseRepository.findAllByOrderByNameAsc()).thenReturn(List.of(exercise1,exercise2));
        List<ExerciseResponse> exercises = exerciseService.findAll();
        assertAll(
                () -> assertEquals(2, exercises.size()),
                () -> assertEquals(exerciseId1, exercises.getFirst().id()),
                () -> assertEquals(exercise1.getName(), exercises.getFirst().name()),
                () -> assertEquals(exercise1.getDescription(), exercises.getFirst().description()),
                () -> assertEquals(exercise1.getMuscleGroup(), exercises.getFirst().muscleGroup()),
                () -> assertEquals(exercise1.getEquipment(), exercises.getFirst().equipment()),
                () -> assertEquals(userId, exercises.getFirst().createdBy())
        );
        verify(exerciseRepository).findAllByOrderByNameAsc();

    }
    @Test
    void shouldFindExerciseById() {
        UUID exerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User currentUser = new User(
                "Test",
                "Test",
                "test@example.com",
                "hashed-password"
        );
        currentUser.setId(userId);

        Exercise exercise = new Exercise(
                "Test",
                "Description",
                "Muscle",
                "Equipment",
                currentUser
        );
        exercise.setId(exerciseId);

        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.of(exercise));

        Optional<ExerciseResponse> result =
                exerciseService.findById(exerciseId);

        assertTrue(result.isPresent());

        ExerciseResponse response = result.orElseThrow();

        assertAll(
                () -> assertEquals(exerciseId, response.id()),
                () -> assertEquals("Test", response.name()),
                () -> assertEquals("Description", response.description()),
                () -> assertEquals("Muscle", response.muscleGroup()),
                () -> assertEquals("Equipment", response.equipment()),
                () -> assertEquals(userId, response.createdBy())
        );

        verify(exerciseRepository).findById(exerciseId);
    }
    @Test
    void shouldReturnEmptyWhenExerciseNotFound() {
        UUID exerciseId = UUID.randomUUID();

        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.empty());

        Optional<ExerciseResponse> result =
                exerciseService.findById(exerciseId);

        assertTrue(result.isEmpty());

        verify(exerciseRepository).findById(exerciseId);
    }
}
