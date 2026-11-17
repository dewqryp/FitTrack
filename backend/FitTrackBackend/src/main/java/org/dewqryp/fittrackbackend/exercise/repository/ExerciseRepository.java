package org.dewqryp.fittrackbackend.exercise.repository;

import org.dewqryp.fittrackbackend.exercise.entity.Exercise;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {
    List<Exercise> findAllByOrderByNameAsc();
}
