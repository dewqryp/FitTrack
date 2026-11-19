package org.dewqryp.fittrackbackend.workout.controller;

import jakarta.validation.Valid;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workout.dto.WorkoutPlanCreateRequest;
import org.dewqryp.fittrackbackend.workout.dto.WorkoutPlanResponse;
import org.dewqryp.fittrackbackend.workout.service.WorkoutPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wprkout-plans")
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    public WorkoutPlanController(WorkoutPlanService workoutPlanService) {
        this.workoutPlanService = workoutPlanService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkoutPlanResponse> create(@Valid @RequestBody WorkoutPlanCreateRequest request,@AuthenticationPrincipal User owner) {
        WorkoutPlanResponse response = workoutPlanService.create(request,owner);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<WorkoutPlanResponse> get(@AuthenticationPrincipal User owner) {
        return workoutPlanService.findAllByOwner(owner);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkoutPlanResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.of(workoutPlanService.findById(id));
    }
}
