package org.dewqryp.fittrackbackend.exercise.service;

import org.dewqryp.fittrackbackend.exercise.dto.ExerciseCreateRequest;
import org.dewqryp.fittrackbackend.exercise.dto.ExerciseResponse;
import org.dewqryp.fittrackbackend.exercise.entity.Exercise;
import org.dewqryp.fittrackbackend.exercise.repository.ExerciseRepository;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;


    public ExerciseService(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    public ExerciseResponse create(ExerciseCreateRequest exerciseCreateRequest, User currentUser) {
        Exercise exercise = exerciseRepository.save(new Exercise(exerciseCreateRequest.name(), exerciseCreateRequest.description(), exerciseCreateRequest.muscleGroup(), exerciseCreateRequest.equipment(), currentUser));
        return toResponse(exercise);

    }

    private ExerciseResponse toResponse(Exercise exercise) {
        return new ExerciseResponse(exercise.getId(), exercise.getName(), exercise.getDescription(), exercise.getMuscleGroup(), exercise.getEquipment(), exercise.getCreatedBy().getId());
    }

    public List<ExerciseResponse> findAll() {
        return exerciseRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    public Optional<ExerciseResponse> findById(UUID id){
        return exerciseRepository.findById(id).map(this::toResponse);
    }



}
