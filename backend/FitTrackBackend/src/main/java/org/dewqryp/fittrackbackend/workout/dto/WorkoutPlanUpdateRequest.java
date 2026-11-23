package org.dewqryp.fittrackbackend.workout.dto;

import jakarta.validation.constraints.NotBlank;

public record WorkoutPlanUpdateRequest(@NotBlank String name,@NotBlank String description) {
}
