package org.dewqryp.fittrackbackend.workoutplanexercises.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WorkoutPlanExerciseUpdateRequest(@Min(1) @NotNull Integer sets, @Min(1) @NotNull Integer reps, @Min(0) @NotNull Integer restSeconds, @Min(0) @NotNull Integer orderIndex) {
}
