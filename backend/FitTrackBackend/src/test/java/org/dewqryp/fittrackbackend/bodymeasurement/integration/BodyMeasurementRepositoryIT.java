package org.dewqryp.fittrackbackend.bodymeasurement.integration;

import org.dewqryp.fittrackbackend.bodymeasurement.entity.BodyMeasurement;
import org.dewqryp.fittrackbackend.bodymeasurement.repository.BodyMeasurementRepository;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class BodyMeasurementRepositoryIT {

    @Autowired
    private BodyMeasurementRepository  bodyMeasurementRepository;
    @Autowired
    private UserRepository userRepository;
    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("fittrack_test")
                    .withUsername("test")
                    .withPassword("test");
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldSaveBodyMeasurement(){
        User user = new User("User", "User", "user@example.com", "hashed-password");
        User savedUser = userRepository.save(user);
        BodyMeasurement bodyMeasurement = new BodyMeasurement(savedUser, BigDecimal.valueOf(57.5), 12, "Integration Test");
        BodyMeasurement savedBodyMeasurement = bodyMeasurementRepository.save(bodyMeasurement);
        assertAll(
                () -> assertNotNull(savedBodyMeasurement.getId()),
                () -> assertEquals(savedUser.getId(), savedBodyMeasurement.getUser().getId()),
                () -> assertEquals(savedUser.getId(), savedBodyMeasurement.getUser().getId()),
                () -> assertEquals(0, BigDecimal.valueOf(57.5).compareTo(savedBodyMeasurement.getWeight())),
                () -> assertEquals(12, savedBodyMeasurement.getBodyFatPercentage()),
                () -> assertNotNull(savedBodyMeasurement.getMeasuredAt()),
                () -> assertEquals("Integration Test", savedBodyMeasurement.getNotes())
        );

    }
    @Test
    void shouldReturnBodyMeasurementsOrderedByMeasuredAtDesc() {
        User user = new User(
                "Test",
                "User",
                "sort-" + UUID.randomUUID() + "@example.com",
                "hashed-password"
        );

        User savedUser = userRepository.save(user);

        BodyMeasurement older = new BodyMeasurement(
                savedUser,
                BigDecimal.valueOf(57.0),
                12,
                "Older measurement"
        );
        older.setMeasuredAt(OffsetDateTime.now().minusDays(2));

        BodyMeasurement newer = new BodyMeasurement(
                savedUser,
                BigDecimal.valueOf(58.0),
                13,
                "Newer measurement"
        );
        newer.setMeasuredAt(OffsetDateTime.now());

        bodyMeasurementRepository.saveAll(List.of(older, newer));

        List<BodyMeasurement> measurements =
                bodyMeasurementRepository
                        .findAllByUser_IdOrderByMeasuredAtDesc(savedUser.getId());

        assertAll(
                () -> assertEquals(2, measurements.size()),
                () -> assertEquals("Newer measurement", measurements.get(0).getNotes()),
                () -> assertEquals("Older measurement", measurements.get(1).getNotes())
        );
    }
    @Test
    void shouldReturnOnlyMeasurementsForRequestedUser(){
        User firstUser = userRepository.save(new User("First", "User", "first-"+UUID.randomUUID().toString() +"@example.com", "hashed-password"));
        User secondUser = userRepository.save(new User("Second", "User", "second-"+UUID.randomUUID().toString() +"@example.com", "hashed-password"));
        BodyMeasurement firstBodyMeasurement = new BodyMeasurement(firstUser, BigDecimal.valueOf(57.5), 12, "First user Test");
        BodyMeasurement secondBodyMeasurement = new BodyMeasurement(secondUser, BigDecimal.valueOf(80.0), 20, "Second User Test");
        bodyMeasurementRepository.saveAll(List.of(firstBodyMeasurement, secondBodyMeasurement));
        List<BodyMeasurement> results = bodyMeasurementRepository.findAllByUser_IdOrderByMeasuredAtDesc(firstUser.getId());
        assertAll(
                () -> assertEquals(1, results.size()),
                () -> assertEquals(firstUser.getId(), results.getFirst().getUser().getId()),
                () -> assertEquals("First user Test", results.getFirst().getNotes())
        );


    }
}
