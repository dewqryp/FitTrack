package org.dewqryp.fittrackbackend.exercise.dto;

import org.dewqryp.fittrackbackend.exercise.repository.ExerciseRepository;

import java.util.UUID;

public record ExerciseResponse(UUID id, String name, String description, String muscleGroup, String equipment, UUID createdBy) {
}
