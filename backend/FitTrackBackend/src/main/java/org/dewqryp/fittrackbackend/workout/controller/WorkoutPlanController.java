package org.dewqryp.fittrackbackend.workout.controller;

import jakarta.validation.Valid;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workout.dto.WorkoutPlanCreateRequest;
import org.dewqryp.fittrackbackend.workout.dto.WorkoutPlanResponse;
import org.dewqryp.fittrackbackend.workout.dto.WorkoutPlanUpdateRequest;
import org.dewqryp.fittrackbackend.workout.service.WorkoutPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workout-plans")
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    public WorkoutPlanController(WorkoutPlanService workoutPlanService) {
        this.workoutPlanService = workoutPlanService;
    }

    @PostMapping
    public ResponseEntity<WorkoutPlanResponse> create(@Valid @RequestBody WorkoutPlanCreateRequest request,@AuthenticationPrincipal User owner) {
        WorkoutPlanResponse response = workoutPlanService.create(request,owner);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<WorkoutPlanResponse> getWorkoutPlans(@AuthenticationPrincipal User owner) {
        return workoutPlanService.findAllByOwner(owner);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutPlanResponse> getById(@PathVariable UUID id, @AuthenticationPrincipal User owner) {
        return ResponseEntity.of(workoutPlanService.findById(id, owner));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id, @AuthenticationPrincipal User owner) {
        workoutPlanService.deleteWorkoutPlan(id, owner);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutPlanResponse> updateById(@PathVariable UUID id, @Valid @RequestBody WorkoutPlanUpdateRequest request, @AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(workoutPlanService.updateWorkoutPlan(id, request, owner));
    }

}
