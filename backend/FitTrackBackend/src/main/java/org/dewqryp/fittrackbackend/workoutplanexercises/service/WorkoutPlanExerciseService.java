package org.dewqryp.fittrackbackend.workoutplanexercises.service;

import org.dewqryp.fittrackbackend.exercise.entity.Exercise;
import org.dewqryp.fittrackbackend.exercise.repository.ExerciseRepository;
import org.dewqryp.fittrackbackend.workout.entity.WorkoutPlan;
import org.dewqryp.fittrackbackend.workout.repository.WorkoutPlanRepository;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseCreateRequest;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseResponse;
import org.dewqryp.fittrackbackend.workoutplanexercises.entity.WorkoutPlanExercise;
import org.dewqryp.fittrackbackend.workoutplanexercises.repository.WorkoutPlanExerciseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WorkoutPlanExerciseService {
    private final WorkoutPlanExerciseRepository workoutPlanExerciseRepository;
    private  final WorkoutPlanRepository workoutPlanRepository;
    private final ExerciseRepository exerciseRepository;
    public WorkoutPlanExerciseService(WorkoutPlanExerciseRepository workoutPlanExerciseRepository, WorkoutPlanRepository workoutPlanRepository, ExerciseRepository exerciseRepository) {
        this.workoutPlanExerciseRepository = workoutPlanExerciseRepository;
        this.workoutPlanRepository = workoutPlanRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public WorkoutPlanExerciseResponse addExerciseToWorkoutPlan(UUID workoutPlanId, WorkoutPlanExerciseCreateRequest createRequest) {
        WorkoutPlan workoutPlan = workoutPlanRepository.findById(workoutPlanId).orElseThrow(() -> new IllegalArgumentException("Workout Plan Not Found"));
        Exercise exercise = exerciseRepository.findById(createRequest.exerciseId()).orElseThrow(() -> new IllegalArgumentException("Exercise Not Found"));
        WorkoutPlanExercise workoutPlanExercise = new WorkoutPlanExercise(workoutPlan, exercise, createRequest.orderIndex())

    }

}
