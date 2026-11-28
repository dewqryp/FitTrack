package org.dewqryp.fittrackbackend.workoutsession.integration;

import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.user.repository.UserRepository;
import org.dewqryp.fittrackbackend.workoutsession.entity.WorkoutSession;
import org.dewqryp.fittrackbackend.workoutsession.repository.WorkoutSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class WorkoutSessionRepositoryIT {
    @Autowired
    private WorkoutSessionRepository workoutSessionRepository;

    @Autowired
    private UserRepository userRepository;
    @Container
    static PostgreSQLContainer container = new PostgreSQLContainer("postgres:16-alpine")
            .withUsername("test")
            .withPassword("test")
            .withDatabaseName("fittrack-test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    @Test
    void shouldSaveWorkoutSessionWithoutWorkoutPlan(){
        User user = userRepository.save(new User("User", "User", "user-"+ UUID.randomUUID().toString() +"@example.com", "hashed-password"));
        WorkoutSession workoutSession = new WorkoutSession(user, null, "Workout session integration test");
        workoutSession.setStartedAt(OffsetDateTime.now().minusDays(1));
        WorkoutSession savedWorkoutSession = workoutSessionRepository.save(workoutSession);
        assertAll(
                () -> assertEquals(user.getId(), savedWorkoutSession.getUser().getId()),
                () -> assertEquals("Workout session integration test", savedWorkoutSession.getNotes()),
                () -> assertNull(savedWorkoutSession.getWorkoutPlan()),
                () -> assertNotNull(savedWorkoutSession.getStartedAt()),
                () -> assertNotNull(savedWorkoutSession.getId()),
                () -> assertNull(savedWorkoutSession.getFinishedAt())
        );

    }

    @Test
    void shouldReturnWorkoutSessionsOrderedByStartedAtDesc(){
        User user = userRepository.save(new User("User", "User", "user-"+ UUID.randomUUID().toString() +"@example.com", "hashed-password"));
        WorkoutSession olderWorkoutSession = new WorkoutSession(user, null, "Older Workout session");
        WorkoutSession newerWorkoutSession = new WorkoutSession(user, null, "Newer Workout session");
        olderWorkoutSession.setStartedAt(OffsetDateTime.now().minusDays(2));
        newerWorkoutSession.setStartedAt(OffsetDateTime.now().minusDays(1));
        workoutSessionRepository.saveAll(List.of(olderWorkoutSession, newerWorkoutSession));
        List<WorkoutSession> result = workoutSessionRepository.findAllByUser_IdOrderByStartedAtDesc(user.getId());
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("Older Workout session", result.getLast().getNotes()),
                () -> assertEquals("Newer Workout session", result.getFirst().getNotes())
        );


    }

    @Test
    void shouldReturnOnlyWorkoutSessionsForRequestedUser(){
        User firstUser = userRepository.save(new User("First", "User", "first-"+ UUID.randomUUID().toString() +"@example.com", "hashed-password"));
        User secondUser = userRepository.save(new User("Second", "User", "second-"+ UUID.randomUUID().toString() +"@example.com", "hashed-password"));
        WorkoutSession firstWorkoutSession = new WorkoutSession(firstUser, null, "Workout session integration test");
        firstWorkoutSession.setStartedAt(OffsetDateTime.now().minusDays(1));
        WorkoutSession firstWorkoutSession1 = new WorkoutSession(firstUser, null, "Workout session integration test");
        firstWorkoutSession1.setStartedAt(OffsetDateTime.now().minusDays(2));
        WorkoutSession secondWorkoutSession = new WorkoutSession(secondUser, null, "Workout session integration test");
        secondWorkoutSession.setStartedAt(OffsetDateTime.now().minusDays(1));
        workoutSessionRepository.saveAll(List.of(firstWorkoutSession, secondWorkoutSession, firstWorkoutSession1));
        List<WorkoutSession> workoutSession = workoutSessionRepository.findAllByUser_IdOrderByStartedAtDesc(firstUser.getId());
        assertAll(
                () -> assertEquals(2, workoutSession.size()),
                () -> assertEquals(firstUser.getId(), workoutSession.get(0).getUser().getId()),
                () -> assertEquals(firstUser.getId(), workoutSession.get(1).getUser().getId()),
                () -> assertEquals(firstWorkoutSession.getId(), workoutSession.get(0).getId()),
                () -> assertEquals(firstWorkoutSession1.getId(), workoutSession.get(1).getId())
        );
        

    }

}
