package org.dewqryp.fittrackbackend.workoutplanexercises.entity;

import jakarta.persistence.*;
import lombok.*;
import org.dewqryp.fittrackbackend.exercise.entity.Exercise;
import org.dewqryp.fittrackbackend.workout.entity.WorkoutPlan;

import java.util.UUID;

@Entity
@Table(name = "workout_plan_exercises")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutPlanExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_plan_id", nullable = false)
    private WorkoutPlan workoutPlan;
    @ManyToOne(fetch = FetchType.LAZY,  optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;
    @Column(name = "order_index", nullable = false)
    private int orderIndex;
    @Column(nullable = false)
    private int sets;
    @Column(nullable = false)
    private int reps;
    @Column(nullable = false)
    private int restSeconds;

    public WorkoutPlanExercise(WorkoutPlan workoutPlan, Exercise exercise, int orderIndex, int sets, int reps, int restSeconds) {
        this.workoutPlan = workoutPlan;
        this.exercise = exercise;
        this.orderIndex = orderIndex;
        this.sets = sets;
        this.reps = reps;
        this.restSeconds = restSeconds;
    }
}
