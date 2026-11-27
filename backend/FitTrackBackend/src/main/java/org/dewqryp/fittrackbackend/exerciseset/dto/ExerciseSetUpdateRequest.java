package org.dewqryp.fittrackbackend.exerciseset.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ExerciseSetUpdateRequest(@Min(1) int setNumber, @Min(1) int reps, @NotNull @DecimalMin("0.0")BigDecimal weight) {
}
