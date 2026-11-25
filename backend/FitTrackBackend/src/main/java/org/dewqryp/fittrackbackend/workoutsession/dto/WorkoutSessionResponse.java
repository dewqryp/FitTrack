package org.dewqryp.fittrackbackend.workoutsession.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkoutSessionResponse(UUID id, UUID workoutPlanId, OffsetDateTime startedAt, OffsetDateTime finishedAt, String notes) {
}
