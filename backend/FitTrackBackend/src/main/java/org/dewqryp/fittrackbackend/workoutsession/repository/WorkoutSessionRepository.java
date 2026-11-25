package org.dewqryp.fittrackbackend.workoutsession.repository;

import org.dewqryp.fittrackbackend.workoutsession.entity.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, UUID> {
    boolean existsByUser_IdAndFinishedAtIsNull(UUID userId);
    Optional<WorkoutSession> findByIdAndUser_Id(UUID id, UUID userId);
    List<WorkoutSession> findAllByUser_IdOrderByStartedAtDesc(UUID userId);
}
