package org.dewqryp.fittrackbackend.workoutsession.controller;

import jakarta.validation.Valid;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workoutsession.dto.WorkoutSessionCreateRequest;
import org.dewqryp.fittrackbackend.workoutsession.dto.WorkoutSessionResponse;
import org.dewqryp.fittrackbackend.workoutsession.dto.WorkoutSessionUpdateRequest;
import org.dewqryp.fittrackbackend.workoutsession.entity.WorkoutSession;
import org.dewqryp.fittrackbackend.workoutsession.service.WorkoutSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkoutSessionResponse>  getWorkoutSession(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.of(workoutSessionService.getWorkoutSessionById(id,user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkoutSessionResponse> updateWorkoutSession(@PathVariable UUID id, @Valid @RequestBody WorkoutSessionUpdateRequest updateRequest, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(workoutSessionService.updateWorkoutSession(id, updateRequest, user));
    }

    @PostMapping("/{id}/finish")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkoutSessionResponse> finishWorkoutSession(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(workoutSessionService.finishWorkoutSession(id, user));
    }

}
