package org.dewqryp.fittrackbackend.exerciseset.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ExerciseSetResponse(UUID id, UUID workoutSessionId, UUID exerciseId, int setNumber, int reps, BigDecimal weight, OffsetDateTime createdAt) {
}
