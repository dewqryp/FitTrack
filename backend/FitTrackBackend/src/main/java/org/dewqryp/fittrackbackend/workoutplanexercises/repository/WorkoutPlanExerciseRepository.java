package org.dewqryp.fittrackbackend.workoutplanexercises.repository;

import org.dewqryp.fittrackbackend.workoutplanexercises.entity.WorkoutPlanExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkoutPlanExerciseRepository extends JpaRepository<WorkoutPlanExercise, UUID> {
    List<WorkoutPlanExercise> findAllByWorkoutPlanIdOrderByOrderIndexAsc(UUID workoutPlan_id);
}
