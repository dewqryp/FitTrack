package org.dewqryp.fittrackbackend.workoutsession.exceptions;

public class WorkoutSessionIsAlreadyFinishedException extends RuntimeException {
    public WorkoutSessionIsAlreadyFinishedException(String message) {
        super(message);
    }
}
