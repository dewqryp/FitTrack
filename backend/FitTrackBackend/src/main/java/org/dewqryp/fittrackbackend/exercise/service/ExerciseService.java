package org.dewqryp.fittrackbackend.exercise.service;

import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.exercise.dto.ExerciseCreateRequest;
import org.dewqryp.fittrackbackend.exercise.dto.ExerciseResponse;
import org.dewqryp.fittrackbackend.exercise.dto.ExerciseUpdateRequest;
import org.dewqryp.fittrackbackend.exercise.entity.Exercise;
import org.dewqryp.fittrackbackend.exercise.exceptions.ExerciseInUseException;
import org.dewqryp.fittrackbackend.exercise.repository.ExerciseRepository;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.user.repository.UserRepository;
import org.dewqryp.fittrackbackend.workoutplanexercises.repository.WorkoutPlanExerciseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final WorkoutPlanExerciseRepository workoutPlanExerciseRepository;


    public ExerciseService(ExerciseRepository exerciseRepository, WorkoutPlanExerciseRepository workoutPlanExerciseRepository) {
        this.exerciseRepository = exerciseRepository;
        this.workoutPlanExerciseRepository = workoutPlanExerciseRepository;
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

    public ExerciseResponse update(UUID id, ExerciseUpdateRequest exerciseUpdateRequest) {
        Exercise exercise = exerciseRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Exercise not found"));
        exercise.setName(exerciseUpdateRequest.name());
        exercise.setDescription(exerciseUpdateRequest.description());
        exercise.setMuscleGroup(exerciseUpdateRequest.muscleGroup());
        exercise.setEquipment(exerciseUpdateRequest.equipment());
        return toResponse(exerciseRepository.save(exercise));

    }

    public void deleteById(UUID id) {
        Exercise exercise =  exerciseRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Exercise not found"));
        if(workoutPlanExerciseRepository.existsByExercise_Id(exercise.getId())) {
            throw new ExerciseInUseException("Workout Plan Exercise is in use");
        }
        exerciseRepository.delete(exercise);


    }



}
