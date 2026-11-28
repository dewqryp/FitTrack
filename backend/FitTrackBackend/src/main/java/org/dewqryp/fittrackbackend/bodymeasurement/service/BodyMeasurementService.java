package org.dewqryp.fittrackbackend.bodymeasurement.service;

import org.dewqryp.fittrackbackend.bodymeasurement.dto.BodyMeasurementCreateRequest;
import org.dewqryp.fittrackbackend.bodymeasurement.dto.BodyMeasurementResponse;
import org.dewqryp.fittrackbackend.bodymeasurement.entity.BodyMeasurement;
import org.dewqryp.fittrackbackend.bodymeasurement.repository.BodyMeasurementRepository;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BodyMeasurementService {
    private final BodyMeasurementRepository bodyMeasurementRepository;

    public BodyMeasurementService(BodyMeasurementRepository bodyMeasurementRepository) {
        this.bodyMeasurementRepository = bodyMeasurementRepository;
    }
    @Transactional
    public BodyMeasurementResponse createBodyMeasurement(BodyMeasurementCreateRequest bodyMeasurementCreateRequest, User user) {
        BodyMeasurement bodyMeasurement = new BodyMeasurement(user, bodyMeasurementCreateRequest.weight(), bodyMeasurementCreateRequest.bodyFatPercentage(), bodyMeasurementCreateRequest.notes());
        return toResponse(bodyMeasurementRepository.save(bodyMeasurement));
    }
    @Transactional(readOnly = true)
    public List<BodyMeasurementResponse> getAllBodyMeasurements(User user) {
        return bodyMeasurementRepository.findAllByUser_IdOrderByMeasuredAtDesc(user.getId()).stream().map(this::toResponse).toList();
    }

    private BodyMeasurementResponse toResponse(BodyMeasurement bodyMeasurement) {
        return new BodyMeasurementResponse(bodyMeasurement.getId(), bodyMeasurement.getWeight(), bodyMeasurement.getBodyFatPercentage(), bodyMeasurement.getMeasuredAt(), bodyMeasurement.getNotes());
    }
}


