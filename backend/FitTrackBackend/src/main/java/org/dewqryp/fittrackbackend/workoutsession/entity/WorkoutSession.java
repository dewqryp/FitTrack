package org.dewqryp.fittrackbackend.workoutsession.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.workout.entity.WorkoutPlan;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "workout_sessions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "workout_plan_id", nullable = true)
    private WorkoutPlan workoutPlan;
    @Column(nullable = false)
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;
    @Column(columnDefinition = "TEXT")
    private String notes;
}
