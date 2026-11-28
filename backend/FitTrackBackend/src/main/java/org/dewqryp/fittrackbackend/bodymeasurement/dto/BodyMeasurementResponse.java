package org.dewqryp.fittrackbackend.bodymeasurement.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BodyMeasurementResponse(UUID id, BigDecimal weight, Integer bodyFatPercentage, OffsetDateTime measuredAt, String notes) {
}
