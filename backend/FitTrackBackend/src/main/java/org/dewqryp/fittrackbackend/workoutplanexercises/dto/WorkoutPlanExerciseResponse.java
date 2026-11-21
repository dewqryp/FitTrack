package org.dewqryp.fittrackbackend.workoutplanexercises.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record WorkoutPlanExerciseResponse(UUID id,UUID exerciseId, Integer sets, Integer reps, Integer restSeconds,Integer orderIndex) {
}
