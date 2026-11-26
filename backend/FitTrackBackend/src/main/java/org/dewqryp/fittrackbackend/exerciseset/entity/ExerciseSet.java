package org.dewqryp.fittrackbackend.exerciseset.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dewqryp.fittrackbackend.exercise.entity.Exercise;
import org.dewqryp.fittrackbackend.workoutsession.entity.WorkoutSession;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "exercise_sets")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseSet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_session_id", nullable = false)
    private WorkoutSession workoutSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;
    @Min(1)
    @Column(nullable = false)
    private int setNumber;
    @Column(nullable = false)
    @Min(1)
    private int reps;
    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal weight;
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public ExerciseSet(WorkoutSession workoutSession, Exercise exercise, int setNumber, int reps, BigDecimal weight) {
        this.workoutSession = workoutSession;
        this.exercise = exercise;
        this.setNumber = setNumber;
        this.reps = reps;
        this.weight = weight;
    }
}
