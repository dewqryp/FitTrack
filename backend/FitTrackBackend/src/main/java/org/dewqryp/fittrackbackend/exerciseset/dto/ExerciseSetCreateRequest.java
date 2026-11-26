package org.dewqryp.fittrackbackend.exerciseset.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ExerciseSetCreateRequest(@NotNull UUID exerciseId, @Min(1) int setNumber, @Min(1) int reps, @DecimalMin("0.0") @NotNull BigDecimal weight) {
}
