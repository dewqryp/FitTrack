package org.dewqryp.fittrackbackend.exerciseset.repository;

import org.dewqryp.fittrackbackend.exerciseset.entity.ExerciseSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExerciseSetRepository extends JpaRepository<ExerciseSet, UUID> {
    Optional<ExerciseSet> findByIdAndWorkoutSession_IdAndWorkoutSession_User_Id(UUID id, UUID workoutSessionId, UUID userId);

    List<ExerciseSet> findAllByWorkoutSession_IdOrderBySetNumberAsc(UUID workoutSessionId);
    boolean existsByWorkoutSession_IdAndExercise_IdAndSetNumber(UUID workoutSessionId, UUID exerciseId, int setNumber);
    boolean existsByWorkoutSession_IdAndExercise_IdAndSetNumberAndIdNot(UUID workoutSessionId, UUID exerciseId, int setNumber, UUID id);

}
