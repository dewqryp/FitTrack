package org.dewqryp.fittrackbackend.bodymeasurement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dewqryp.fittrackbackend.user.entity.User;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "body_measurements")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BodyMeasurement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 6,  scale = 2)
    private BigDecimal weight;
    @Min(0)
    @Max(100)
    @Column
    private Integer bodyFatPercentage;
    @Column(nullable = false, updatable = false)
    private OffsetDateTime measuredAt;
    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (measuredAt == null) {
            measuredAt = OffsetDateTime.now();
        }
    }

    public BodyMeasurement(User user, BigDecimal weight, Integer bodyFatPercentage, String notes) {
        this.user = user;
        this.weight = weight;
        this.bodyFatPercentage = bodyFatPercentage;
        this.notes = notes;
    }
}
