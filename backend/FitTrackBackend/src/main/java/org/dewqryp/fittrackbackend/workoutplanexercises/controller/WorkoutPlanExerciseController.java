package org.dewqryp.fittrackbackend.workoutplanexercises.controller;

import jakarta.validation.Valid;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseCreateRequest;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseResponse;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseUpdateRequest;
import org.dewqryp.fittrackbackend.workoutplanexercises.service.WorkoutPlanExerciseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workout-plans/{workoutPlanId}/exercises")
public class WorkoutPlanExerciseController {
    private final WorkoutPlanExerciseService workoutPlanExerciseService;

    public WorkoutPlanExerciseController(WorkoutPlanExerciseService workoutPlanExerciseService) {
        this.workoutPlanExerciseService = workoutPlanExerciseService;
    }

    @PostMapping
    public WorkoutPlanExerciseResponse addExerciseToWorkoutPlan(@PathVariable UUID workoutPlanId,@Valid @RequestBody WorkoutPlanExerciseCreateRequest request) {
        return workoutPlanExerciseService.addExerciseToWorkoutPlan(workoutPlanId, request);
    }

    @GetMapping
    public List<WorkoutPlanExerciseResponse> getExercises(@PathVariable UUID workoutPlanId) {
        return workoutPlanExerciseService.getExercisesByWorkoutPlanId(workoutPlanId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutPlanExerciseResponse> getExercise(@PathVariable UUID workoutPlanId, @PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.of(workoutPlanExerciseService.getWorkoutPlanExerciseById(id, workoutPlanId, user));
    }

    @PutMapping("/{id}")
    public WorkoutPlanExerciseResponse updateWorkoutPlanExercise(@PathVariable UUID workoutPlanId, @PathVariable("id") UUID workoutPlanExerciseId, @Valid @RequestBody WorkoutPlanExerciseUpdateRequest request) {
        return workoutPlanExerciseService.updateWorkoutPlanExercise(workoutPlanId, workoutPlanExerciseId, request);
    }

    @DeleteMapping("/{id}")
    public void deleteWorkoutPlanExercise(@PathVariable UUID workoutPlanId, @PathVariable("id") UUID workoutPlanExerciseId) {
        workoutPlanExerciseService.deleteWorkoutPlanExercise(workoutPlanId, workoutPlanExerciseId);
    }

}


