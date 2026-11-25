package org.dewqryp.fittrackbackend.workoutsession.dto;

import java.util.UUID;

public record WorkoutSessionCreateRequest(UUID workoutPlanId, String notes) {
}
