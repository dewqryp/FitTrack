package org.dewqryp.fittrackbackend.exercise.controller;

import jakarta.validation.Valid;
import org.dewqryp.fittrackbackend.exercise.dto.ExerciseCreateRequest;
import org.dewqryp.fittrackbackend.exercise.dto.ExerciseResponse;
import org.dewqryp.fittrackbackend.exercise.service.ExerciseService;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exercises")
public class ExerciseController {
    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ExerciseResponse> getExercises() {
        return exerciseService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExerciseResponse> getExercise(@PathVariable UUID id) {
        return ResponseEntity.of(exerciseService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<ExerciseResponse> createExercise(@Valid @RequestBody ExerciseCreateRequest exerciseCreateRequest, @AuthenticationPrincipal User user) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authentication: " + auth);
        System.out.println("Authorities: " + auth.getAuthorities());
        ExerciseResponse response = exerciseService.create(exerciseCreateRequest, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
