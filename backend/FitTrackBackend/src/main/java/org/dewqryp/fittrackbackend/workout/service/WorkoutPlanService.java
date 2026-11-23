package org.dewqryp.fittrackbackend.workout.service;

import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workout.dto.WorkoutPlanCreateRequest;
import org.dewqryp.fittrackbackend.workout.dto.WorkoutPlanResponse;
import org.dewqryp.fittrackbackend.workout.dto.WorkoutPlanUpdateRequest;
import org.dewqryp.fittrackbackend.workout.entity.WorkoutPlan;
import org.dewqryp.fittrackbackend.workout.repository.WorkoutPlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;


    public WorkoutPlanService(WorkoutPlanRepository workoutPlanRepository) {
        this.workoutPlanRepository = workoutPlanRepository;
    }

    public WorkoutPlanResponse create(WorkoutPlanCreateRequest request, User owner) {

        WorkoutPlan workoutPlan = new WorkoutPlan(request.name(), request.description(), owner);
        return toResponse(workoutPlanRepository.save(workoutPlan));

    }

    public List<WorkoutPlanResponse> findAllByOwner(User owner) {
        return workoutPlanRepository.findAllByOwnerOrderByCreatedAtDesc(owner).stream().map(this::toResponse).toList();
    }

    public Optional<WorkoutPlanResponse> findById(UUID id, User owner) {
        return workoutPlanRepository.findByIdAndOwner_Id(id, owner.getId()).map(this::toResponse);
    }

    public void deleteWorkoutPlan(UUID id, User owner) {
        WorkoutPlan workoutPlan = workoutPlanRepository.findByIdAndOwner_Id(id, owner.getId()).orElseThrow(() -> new EntityNotFoundException("Workout Plan with id " + id + " does not exist"));
        workoutPlanRepository.delete(workoutPlan);
    }

    public WorkoutPlanResponse updateWorkoutPlan(UUID id, WorkoutPlanUpdateRequest request, User owner) {
        WorkoutPlan workoutPlan = workoutPlanRepository.findByIdAndOwner_Id(id, owner.getId()).orElseThrow(() -> new EntityNotFoundException("Workout Plan with id " + id + " does not exist"));
        workoutPlan.setName(request.name());
        workoutPlan.setDescription(request.description());
        return toResponse(workoutPlanRepository.save(workoutPlan));
    }

    private WorkoutPlanResponse toResponse(WorkoutPlan workoutPlan) {
        return new WorkoutPlanResponse(workoutPlan.getId(), workoutPlan.getName(), workoutPlan.getDescription(), workoutPlan.getOwner().getId(), workoutPlan.getCreatedAt());
    }


}
