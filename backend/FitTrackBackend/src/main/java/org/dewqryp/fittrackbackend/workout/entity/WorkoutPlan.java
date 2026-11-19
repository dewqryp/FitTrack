package org.dewqryp.fittrackbackend.workout.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dewqryp.fittrackbackend.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "workout_plans")
public class WorkoutPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id",  nullable = false)
    private User owner;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public WorkoutPlan(String name, String description, User owner) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        createdAt = LocalDateTime.now();
    }


}
