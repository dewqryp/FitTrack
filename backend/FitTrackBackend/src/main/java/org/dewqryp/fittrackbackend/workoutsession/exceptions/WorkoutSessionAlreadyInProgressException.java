package org.dewqryp.fittrackbackend.workoutsession.exceptions;

public class WorkoutSessionAlreadyInProgressException extends RuntimeException {
    public WorkoutSessionAlreadyInProgressException(String message) {
        super(message);
    }
}
