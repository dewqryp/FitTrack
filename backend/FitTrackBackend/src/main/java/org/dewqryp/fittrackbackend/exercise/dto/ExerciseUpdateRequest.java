package org.dewqryp.fittrackbackend.exercise.dto;

import jakarta.validation.constraints.NotBlank;

public record ExerciseUpdateRequest(@NotBlank String name, @NotBlank String description, @NotBlank String muscleGroup, String equipment) {
}
