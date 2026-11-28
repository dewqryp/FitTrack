package org.dewqryp.fittrackbackend.bodymeasurement.controller;

import jakarta.validation.Valid;
import org.dewqryp.fittrackbackend.bodymeasurement.dto.BodyMeasurementCreateRequest;
import org.dewqryp.fittrackbackend.bodymeasurement.dto.BodyMeasurementResponse;
import org.dewqryp.fittrackbackend.bodymeasurement.service.BodyMeasurementService;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/body-measurements")
public class BodyMeasurementController {
    private final BodyMeasurementService bodyMeasurementService;

    public BodyMeasurementController(BodyMeasurementService bodyMeasurementService) {
        this.bodyMeasurementService = bodyMeasurementService;
    }
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BodyMeasurementResponse>> getAllBodyMeasurements(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(this.bodyMeasurementService.getAllBodyMeasurements(user));
    }
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BodyMeasurementResponse> createBodyMeasurement(@Valid @RequestBody BodyMeasurementCreateRequest createRequest, @AuthenticationPrincipal User user){
        return ResponseEntity.status(HttpStatus.CREATED).body(bodyMeasurementService.createBodyMeasurement(createRequest,user));
    }
}
