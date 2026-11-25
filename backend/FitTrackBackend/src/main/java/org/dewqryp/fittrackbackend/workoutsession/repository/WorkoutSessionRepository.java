package org.dewqryp.fittrackbackend.workoutsession.repository;

import org.dewqryp.fittrackbackend.workoutsession.entity.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, UUID> {
    boolean existsByUser_IdAndFinishedAtIsNull(UUID userId);
}
