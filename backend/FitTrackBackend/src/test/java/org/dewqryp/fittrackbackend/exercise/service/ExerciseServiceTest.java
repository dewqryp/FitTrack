package org.dewqryp.fittrackbackend.exercise.service;

import org.dewqryp.fittrackbackend.exercise.dto.ExerciseCreateRequest;
import org.dewqryp.fittrackbackend.exercise.dto.ExerciseResponse;
import org.dewqryp.fittrackbackend.exercise.entity.Exercise;
import org.dewqryp.fittrackbackend.exercise.repository.ExerciseRepository;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
        currentUser.setId(userId);
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
    }
}
