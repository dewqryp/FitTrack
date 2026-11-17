package org.dewqryp.fittrackbackend.exercise.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dewqryp.fittrackbackend.user.entity.User;

import java.util.UUID;

@Entity
@Table(name = "exercises")
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private String muscleGroup;
    private String equipment;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;


}
