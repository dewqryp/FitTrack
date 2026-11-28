package org.dewqryp.fittrackbackend.bodymeasurement.repository;

import org.dewqryp.fittrackbackend.bodymeasurement.entity.BodyMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BodyMeasurementRepository extends JpaRepository<BodyMeasurement, UUID> {
    List<BodyMeasurement> findAllByUser_IdOrderByMeasuredAtDesc(UUID userId);
    Optional<BodyMeasurement> findByIdAndUserId(UUID id, UUID userId);
}
