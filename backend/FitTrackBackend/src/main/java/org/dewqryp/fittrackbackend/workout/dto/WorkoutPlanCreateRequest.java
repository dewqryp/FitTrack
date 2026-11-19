package org.dewqryp.fittrackbackend.workout.dto;

import jakarta.validation.constraints.NotBlank;

public record WorkoutPlanCreateRequest(@NotBlank String name,@NotBlank String description) {
}
