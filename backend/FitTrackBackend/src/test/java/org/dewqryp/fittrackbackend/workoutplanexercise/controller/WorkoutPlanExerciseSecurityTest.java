package org.dewqryp.fittrackbackend.workoutplanexercise.controller;

import org.dewqryp.fittrackbackend.auth.jwt.JwtService;
import org.dewqryp.fittrackbackend.config.SecurityConfig;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.user.repository.UserRepository;
import org.dewqryp.fittrackbackend.workoutplanexercises.controller.WorkoutPlanExerciseController;
import org.dewqryp.fittrackbackend.workoutplanexercises.service.WorkoutPlanExerciseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkoutPlanExerciseController.class)
@Import(SecurityConfig.class)
public class WorkoutPlanExerciseSecurityTest {
    @MockitoBean
    private WorkoutPlanExerciseService workoutPlanExerciseService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/workout-plans/{workoutPlanId}/exercises", workoutPlanId))
                        .andExpect(status().isUnauthorized());

        verifyNoInteractions(workoutPlanExerciseService);
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        String token = UUID.randomUUID().toString();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/workout-plans/{workoutPlanId}/exercises", workoutPlanId).header("Authorization","Bearer " + token))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(workoutPlanExerciseService);
    }

    @Test
    void shouldAllowRequestWhenTokenIsValid() throws Exception {
        UUID  workoutPlanId = UUID.randomUUID();
        String email = "test@example.com";
        String token = UUID.randomUUID().toString();

        User user = new User(
                "Test",
                "User",
                email,
                "passwordHash"
        );
        user.setId(UUID.randomUUID());
        when(jwtService.extractEmail(token)).thenReturn(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(jwtService.isTokenValid(token, user))
                .thenReturn(true);
        when(workoutPlanExerciseService.getExercisesByWorkoutPlanId(workoutPlanId))
                .thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/workout-plans/{workoutPlanId}/exercises", workoutPlanId).header("Authorization","Bearer " + token))
                .andExpect(status().isOk());
        verify(jwtService).extractEmail(token);
        verify(userRepository).findByEmail(email);
        verify(jwtService).isTokenValid(token, user);

    }
}
