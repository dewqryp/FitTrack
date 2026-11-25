package org.dewqryp.fittrackbackend.workoutplanexercises.service;

import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.exercise.entity.Exercise;
import org.dewqryp.fittrackbackend.exercise.repository.ExerciseRepository;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workout.entity.WorkoutPlan;
import org.dewqryp.fittrackbackend.workout.repository.WorkoutPlanRepository;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseCreateRequest;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseResponse;
import org.dewqryp.fittrackbackend.workoutplanexercises.dto.WorkoutPlanExerciseUpdateRequest;
import org.dewqryp.fittrackbackend.workoutplanexercises.entity.WorkoutPlanExercise;
import org.dewqryp.fittrackbackend.workoutplanexercises.repository.WorkoutPlanExerciseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

    public WorkoutPlanExerciseResponse addExerciseToWorkoutPlan(UUID workoutPlanId, WorkoutPlanExerciseCreateRequest createRequest, User currentUser) {
        WorkoutPlan workoutPlan = workoutPlanRepository.findByIdAndOwner_Id(workoutPlanId, currentUser.getId()).orElseThrow(() -> new EntityNotFoundException("Workout Plan Not Found"));
        Exercise exercise = exerciseRepository.findById(createRequest.exerciseId()).orElseThrow(() -> new IllegalArgumentException("Exercise Not Found"));
        return toResponse(workoutPlanExerciseRepository.save(new WorkoutPlanExercise(workoutPlan, exercise, createRequest.orderIndex(), createRequest.sets(), createRequest.reps(),  createRequest.restSeconds())));


    }

    public Optional<WorkoutPlanExerciseResponse> getWorkoutPlanExerciseById(UUID workoutPlanExerciseId, UUID workoutPlanId, User currentUser) {
        return workoutPlanExerciseRepository.findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, currentUser.getId()).map(this::toResponse);
    }

    public List<WorkoutPlanExerciseResponse> getExercisesByWorkoutPlanId(UUID workoutPlanId, User currentUser) {
        if(!workoutPlanRepository.existsByIdAndOwner_Id(workoutPlanId, currentUser.getId())) {
            throw new EntityNotFoundException("Workout Plan Not Found");
        }
        return workoutPlanExerciseRepository.findAllByWorkoutPlanIdOrderByOrderIndexAsc(workoutPlanId).stream().map(this::toResponse).toList();
    }

    public WorkoutPlanExerciseResponse updateWorkoutPlanExercise(UUID workoutPlanId, UUID workoutPlanExerciseId, WorkoutPlanExerciseUpdateRequest updateRequest, User currentUser) {
        WorkoutPlanExercise workoutPlanExercise = workoutPlanExerciseRepository.findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, currentUser.getId()).orElseThrow(() -> new EntityNotFoundException("Workout plan exercise Not Found"));
        if(workoutPlanExercise.getWorkoutPlan().getId().equals(workoutPlanId)) {
            workoutPlanExercise.setReps(updateRequest.reps());
            workoutPlanExercise.setOrderIndex(updateRequest.orderIndex());
            workoutPlanExercise.setSets(updateRequest.sets());
            workoutPlanExercise.setRestSeconds(updateRequest.restSeconds());
            return toResponse(workoutPlanExerciseRepository.save(workoutPlanExercise));
        }
        throw new EntityNotFoundException("Workout Plan Exercise Does Not Belong To This Workout Plan");
    }

    public void deleteWorkoutPlanExercise(UUID workoutPlanId, UUID workoutPlanExerciseId, User currentUser) {
        WorkoutPlanExercise workoutPlanExercise = workoutPlanExerciseRepository.findByIdAndWorkoutPlan_IdAndWorkoutPlan_Owner_Id(workoutPlanExerciseId, workoutPlanId, currentUser.getId()).orElseThrow(() -> new EntityNotFoundException("Workout Plan Exercise Not Found"));
        if(workoutPlanExercise.getWorkoutPlan().getId().equals(workoutPlanId)) {
            workoutPlanExerciseRepository.delete(workoutPlanExercise);
        }
        else {
            throw new EntityNotFoundException("Workout Plan Exercise Does Not Belong To This Workout Plan");
        }
    }

    private WorkoutPlanExerciseResponse toResponse(WorkoutPlanExercise workoutPlanExercise) {
        return new WorkoutPlanExerciseResponse(workoutPlanExercise.getId(),workoutPlanExercise.getExercise().getId(), workoutPlanExercise.getSets(), workoutPlanExercise.getReps(), workoutPlanExercise.getRestSeconds(), workoutPlanExercise.getOrderIndex());
    }

}
