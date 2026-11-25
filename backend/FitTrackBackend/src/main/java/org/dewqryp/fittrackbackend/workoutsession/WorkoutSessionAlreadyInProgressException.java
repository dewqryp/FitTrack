package org.dewqryp.fittrackbackend.workoutsession;

public class WorkoutSessionAlreadyInProgressException extends RuntimeException {
    public WorkoutSessionAlreadyInProgressException(String message) {
        super(message);
    }
}
