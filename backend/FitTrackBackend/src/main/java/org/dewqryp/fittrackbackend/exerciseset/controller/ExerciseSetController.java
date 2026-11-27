package org.dewqryp.fittrackbackend.exerciseset.controller;

import jakarta.validation.Valid;
import org.dewqryp.fittrackbackend.exerciseset.dto.ExerciseSetCreateRequest;
import org.dewqryp.fittrackbackend.exerciseset.dto.ExerciseSetResponse;
import org.dewqryp.fittrackbackend.exerciseset.entity.ExerciseSet;
import org.dewqryp.fittrackbackend.exerciseset.service.ExerciseSetService;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workout-sessions/{sessionId}/sets")
public class ExerciseSetController {
    private final ExerciseSetService exerciseSetService;

    public ExerciseSetController(ExerciseSetService exerciseSetService) {
        this.exerciseSetService = exerciseSetService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExerciseSetResponse> createExerciseSet(@PathVariable UUID sessionId, @Valid @RequestBody ExerciseSetCreateRequest request, @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseSetService.createExerciseSet(sessionId, request, user));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ExerciseSetResponse>> getExerciseSets(@PathVariable UUID sessionId, @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.OK).body(exerciseSetService.getExerciseSets(sessionId, user));
    }
}
