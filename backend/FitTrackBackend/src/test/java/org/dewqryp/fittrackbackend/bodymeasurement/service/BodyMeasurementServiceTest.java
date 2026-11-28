package org.dewqryp.fittrackbackend.bodymeasurement.service;

import org.dewqryp.fittrackbackend.bodymeasurement.dto.BodyMeasurementCreateRequest;
import org.dewqryp.fittrackbackend.bodymeasurement.dto.BodyMeasurementResponse;
import org.dewqryp.fittrackbackend.bodymeasurement.entity.BodyMeasurement;
import org.dewqryp.fittrackbackend.bodymeasurement.repository.BodyMeasurementRepository;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BodyMeasurementServiceTest {
    @Mock
    private BodyMeasurementRepository bodyMeasurementRepository;
    @InjectMocks
    private BodyMeasurementService bodyMeasurementService;

    @Test
    void shouldCreateBodyMeasurement() {
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        BodyMeasurementCreateRequest createRequest = new BodyMeasurementCreateRequest(BigDecimal.valueOf(57.5), 12, "Notes");
        when(bodyMeasurementRepository.save(any(BodyMeasurement.class))).thenAnswer(i -> i.getArgument(0));
        BodyMeasurementResponse response = bodyMeasurementService.createBodyMeasurement(createRequest, user);
        assertAll(
                () -> assertEquals(createRequest.bodyFatPercentage(), response.bodyFatPercentage()),
                () -> assertEquals(createRequest.notes(), response.notes()),
                () -> assertEquals(createRequest.weight(), response.weight())
        );
        ArgumentCaptor<BodyMeasurement> captor =
                ArgumentCaptor.forClass(BodyMeasurement.class);

        verify(bodyMeasurementRepository).save(captor.capture());

        BodyMeasurement saved = captor.getValue();

        assertEquals(user.getId(), saved.getUser().getId());
        assertEquals(createRequest.weight(), saved.getWeight());
        assertEquals(createRequest.bodyFatPercentage(), saved.getBodyFatPercentage());
    }

    @Test
    void shouldCreateBodyMeasurementWithoutBodyFatPercentage(){
        UUID userId = UUID.randomUUID();
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);
        BodyMeasurementCreateRequest createRequest = new BodyMeasurementCreateRequest(BigDecimal.valueOf(57.5), null, "Notes");
        when(bodyMeasurementRepository.save(any(BodyMeasurement.class))).thenAnswer(i -> i.getArgument(0));
        BodyMeasurementResponse response = bodyMeasurementService.createBodyMeasurement(createRequest, user);
        assertAll(
                () -> assertNull(response.bodyFatPercentage()),
                () -> assertEquals(createRequest.notes(), response.notes()),
                () -> assertEquals(createRequest.weight(), response.weight())
        );
        ArgumentCaptor<BodyMeasurement> captor =
                ArgumentCaptor.forClass(BodyMeasurement.class);
        verify(bodyMeasurementRepository).save(captor.capture());
        BodyMeasurement saved = captor.getValue();
        assertEquals(user.getId(), saved.getUser().getId());
        assertEquals(createRequest.weight(), saved.getWeight());

    }
    @Test
    void shouldGetAllBodyMeasurements() {
        UUID userId = UUID.randomUUID();

        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);

        BodyMeasurement measurement1 = new BodyMeasurement(
                user, BigDecimal.valueOf(57.5), 12, "First measurement"
        );
        measurement1.setId(UUID.randomUUID());
        measurement1.setMeasuredAt(OffsetDateTime.now().minusDays(2));

        BodyMeasurement measurement2 = new BodyMeasurement(
                user, BigDecimal.valueOf(58.0), 13, "Second measurement"
        );
        measurement2.setId(UUID.randomUUID());
        measurement2.setMeasuredAt(OffsetDateTime.now().minusDays(1));

        BodyMeasurement measurement3 = new BodyMeasurement(
                user, BigDecimal.valueOf(58.5), null, "Third measurement"
        );
        measurement3.setId(UUID.randomUUID());
        measurement3.setMeasuredAt(OffsetDateTime.now());

        when(bodyMeasurementRepository.findAllByUser_IdOrderByMeasuredAtDesc(userId))
                .thenReturn(List.of(measurement3, measurement2, measurement1));

        List<BodyMeasurementResponse> responses =
                bodyMeasurementService.getAllBodyMeasurements(user);

        assertAll(
                () -> assertEquals(3, responses.size()),
                () -> assertEquals(measurement3.getId(), responses.get(0).id()),
                () -> assertEquals(measurement2.getId(), responses.get(1).id()),
                () -> assertEquals(measurement1.getId(), responses.get(2).id()),
                () -> assertEquals(BigDecimal.valueOf(58.5), responses.get(0).weight()),
                () -> assertNull(responses.get(0).bodyFatPercentage()),
                () -> assertEquals("First measurement", responses.get(2).notes())
        );

        verify(bodyMeasurementRepository)
                .findAllByUser_IdOrderByMeasuredAtDesc(userId);
    }
    @Test
    void shouldReturnEmptyListWhenNoBodyMeasurementsExist() {
        UUID userId = UUID.randomUUID();

        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);

        when(bodyMeasurementRepository.findAllByUser_IdOrderByMeasuredAtDesc(userId))
                .thenReturn(List.of());

        List<BodyMeasurementResponse> responses =
                bodyMeasurementService.getAllBodyMeasurements(user);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(bodyMeasurementRepository)
                .findAllByUser_IdOrderByMeasuredAtDesc(userId);
    }
    @Test
    void shouldPreserveRepositoryMeasurementOrder() {
        UUID userId = UUID.randomUUID();

        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(userId);

        BodyMeasurement older = new BodyMeasurement(
                user, BigDecimal.valueOf(56.0), 14, "Older"
        );
        older.setId(UUID.randomUUID());
        older.setMeasuredAt(OffsetDateTime.now().minusDays(5));

        BodyMeasurement newer = new BodyMeasurement(
                user, BigDecimal.valueOf(58.0), 12, "Newer"
        );
        newer.setId(UUID.randomUUID());
        newer.setMeasuredAt(OffsetDateTime.now());

        when(bodyMeasurementRepository.findAllByUser_IdOrderByMeasuredAtDesc(userId))
                .thenReturn(List.of(newer, older));

        List<BodyMeasurementResponse> responses =
                bodyMeasurementService.getAllBodyMeasurements(user);

        assertAll(
                () -> assertEquals(newer.getId(), responses.get(0).id()),
                () -> assertEquals(older.getId(), responses.get(1).id())
        );

        verify(bodyMeasurementRepository)
                .findAllByUser_IdOrderByMeasuredAtDesc(userId);
    }
}
