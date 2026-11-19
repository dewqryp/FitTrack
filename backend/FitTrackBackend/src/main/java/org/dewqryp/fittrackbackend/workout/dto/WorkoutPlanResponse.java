package org.dewqryp.fittrackbackend.workout.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkoutPlanResponse(UUID id,String name, String description, UUID ownerId, LocalDateTime createdAt) {
}
