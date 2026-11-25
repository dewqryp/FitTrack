package org.dewqryp.fittrackbackend.workoutsession.service;

import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workout.entity.WorkoutPlan;
import org.dewqryp.fittrackbackend.workout.repository.WorkoutPlanRepository;
import org.dewqryp.fittrackbackend.workoutsession.dto.WorkoutSessionUpdateRequest;
import org.dewqryp.fittrackbackend.workoutsession.exceptions.WorkoutSessionAlreadyInProgressException;
import org.dewqryp.fittrackbackend.workoutsession.dto.WorkoutSessionCreateRequest;
import org.dewqryp.fittrackbackend.workoutsession.dto.WorkoutSessionResponse;
import org.dewqryp.fittrackbackend.workoutsession.entity.WorkoutSession;
import org.dewqryp.fittrackbackend.workoutsession.exceptions.WorkoutSessionIsAlreadyFinishedException;
import org.dewqryp.fittrackbackend.workoutsession.repository.WorkoutSessionRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class WorkoutSessionService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    public WorkoutSessionService(WorkoutSessionRepository workoutSessionRepository, WorkoutPlanRepository workoutPlanRepository) {
        this.workoutSessionRepository = workoutSessionRepository;
        this.workoutPlanRepository = workoutPlanRepository;
    }

    public WorkoutSessionResponse startWorkoutSession(WorkoutSessionCreateRequest createRequest, User currentUser ) {
        WorkoutPlan workoutPlan = null;
        if(workoutSessionRepository.existsByUser_IdAndFinishedAtIsNull(currentUser.getId())){
            throw new WorkoutSessionAlreadyInProgressException("Workout session already has been started");
        }
        if(createRequest.workoutPlanId() != null)
        {
            workoutPlan = workoutPlanRepository.findByIdAndOwner_Id(createRequest.workoutPlanId(), currentUser.getId()).orElseThrow(EntityNotFoundException::new);
        }
        WorkoutSession workoutSession = new WorkoutSession(currentUser, workoutPlan, createRequest.notes());
        workoutSession.setStartedAt(OffsetDateTime.now());
        return toResponse(workoutSessionRepository.save(workoutSession));

    }

    public WorkoutSessionResponse finishWorkoutSession(UUID sessionId, User currentUser) {
        WorkoutSession session = workoutSessionRepository.findByIdAndUser_Id(sessionId, currentUser.getId()).orElseThrow(EntityNotFoundException::new);
        if(session.getFinishedAt() == null) {
            session.setFinishedAt(OffsetDateTime.now());
            return toResponse(workoutSessionRepository.save(session));
        }
        throw new WorkoutSessionIsAlreadyFinishedException("Workout session has already been finished");
    }

    public WorkoutSessionResponse updateWorkoutSession(UUID sessionId, WorkoutSessionUpdateRequest updateRequest, User currentUser) {
        WorkoutSession workoutSession = workoutSessionRepository.findByIdAndUser_Id(sessionId, currentUser.getId()).orElseThrow(EntityNotFoundException::new);
        if(workoutSession.getFinishedAt() == null) {
            workoutSession.setNotes(updateRequest.notes());
            return toResponse(workoutSessionRepository.save(workoutSession));
        }
        throw new WorkoutSessionIsAlreadyFinishedException("Workout session has already been finished");
    }

    private WorkoutSessionResponse toResponse(WorkoutSession workoutSession) {
        return new WorkoutSessionResponse(workoutSession.getId(), workoutSession.getWorkoutPlan() == null ? null : workoutSession.getWorkoutPlan().getId(), workoutSession.getStartedAt(), workoutSession.getFinishedAt(), workoutSession.getNotes());
    }
}
