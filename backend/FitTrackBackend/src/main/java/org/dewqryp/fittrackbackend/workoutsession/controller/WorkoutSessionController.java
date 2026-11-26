package org.dewqryp.fittrackbackend.workoutsession.controller;

import jakarta.validation.Valid;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workoutsession.dto.WorkoutSessionCreateRequest;
import org.dewqryp.fittrackbackend.workoutsession.dto.WorkoutSessionResponse;
import org.dewqryp.fittrackbackend.workoutsession.entity.WorkoutSession;
import org.dewqryp.fittrackbackend.workoutsession.service.WorkoutSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workout-sessions")
public class WorkoutSessionController {

    private final WorkoutSessionService workoutSessionService;

    public WorkoutSessionController(WorkoutSessionService workoutSessionService) {
        this.workoutSessionService = workoutSessionService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkoutSessionResponse> createWorkoutSession(@Valid @RequestBody WorkoutSessionCreateRequest createRequest, @AuthenticationPrincipal User user) {


        return ResponseEntity.status(HttpStatus.CREATED).body(workoutSessionService.startWorkoutSession(createRequest, user));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WorkoutSessionResponse>> getWorkoutSessions(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(workoutSessionService.getAllWorkoutSessions(user));
    }
}
