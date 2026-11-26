package org.dewqryp.fittrackbackend.exerciseset.service;

import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.exercise.entity.Exercise;
import org.dewqryp.fittrackbackend.exercise.exceptions.ExerciseInUseException;
import org.dewqryp.fittrackbackend.exercise.repository.ExerciseRepository;
import org.dewqryp.fittrackbackend.exerciseset.dto.ExerciseSetCreateRequest;
import org.dewqryp.fittrackbackend.exerciseset.dto.ExerciseSetResponse;
import org.dewqryp.fittrackbackend.exerciseset.entity.ExerciseSet;
import org.dewqryp.fittrackbackend.exerciseset.exception.ExerciseSetAlreadyExistsException;
import org.dewqryp.fittrackbackend.exerciseset.repository.ExerciseSetRepository;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workoutsession.entity.WorkoutSession;
import org.dewqryp.fittrackbackend.workoutsession.exceptions.WorkoutSessionIsAlreadyFinishedException;
import org.dewqryp.fittrackbackend.workoutsession.repository.WorkoutSessionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExerciseSetService {
    private final ExerciseSetRepository exerciseSetRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final ExerciseRepository exerciseRepository;
    public ExerciseSetService(ExerciseSetRepository exerciseSetRepository, WorkoutSessionRepository workoutSessionRepository, ExerciseRepository exerciseRepository) {
        this.exerciseSetRepository = exerciseSetRepository;
        this.workoutSessionRepository = workoutSessionRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public ExerciseSetResponse createExerciseSet(UUID sessionId, ExerciseSetCreateRequest exerciseSetCreateRequest, User currentUser) {
        WorkoutSession workoutSession = workoutSessionRepository.findByIdAndUser_Id(sessionId, currentUser.getId()).orElseThrow(EntityNotFoundException::new);
        if(workoutSession.getFinishedAt() != null){
            throw new WorkoutSessionIsAlreadyFinishedException("Workout session has already finished at: " + workoutSession.getFinishedAt());
        }
        Exercise exercise = exerciseRepository.findById(exerciseSetCreateRequest.exerciseId()).orElseThrow(EntityNotFoundException::new);
        if(exerciseSetRepository.existsByWorkoutSession_IdAndExercise_IdAndSetNumber(workoutSession.getId(), exercise.getId(), exerciseSetCreateRequest.setNumber())){
            throw new ExerciseSetAlreadyExistsException("Exercise set already exists: " + exercise.getName());
        }
        ExerciseSet exerciseSet = new ExerciseSet(workoutSession, exercise, exerciseSetCreateRequest.setNumber(), exerciseSetCreateRequest.reps(), exerciseSetCreateRequest.weight());
        return toResponse(exerciseSetRepository.save(exerciseSet));
    }

    private ExerciseSetResponse toResponse(ExerciseSet exerciseSet) {
        return new ExerciseSetResponse(exerciseSet.getId(), exerciseSet.getWorkoutSession().getId(), exerciseSet.getExercise().getId(), exerciseSet.getSetNumber(), exerciseSet.getReps(), exerciseSet.getWeight(), exerciseSet.getCreatedAt());
    }
}
