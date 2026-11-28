package org.dewqryp.fittrackbackend.bodymeasurement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BodyMeasurementCreateRequest(@NotNull @DecimalMin("0.01")BigDecimal weight, @Min(0) @Max(100) Integer bodyFatPercentage, String notes) {
}
