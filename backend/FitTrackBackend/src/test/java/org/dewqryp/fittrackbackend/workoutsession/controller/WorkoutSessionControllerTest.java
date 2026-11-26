package org.dewqryp.fittrackbackend.workoutsession.controller;

import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.auth.jwt.JwtService;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.user.repository.UserRepository;
import org.dewqryp.fittrackbackend.user.service.UserService;
import org.dewqryp.fittrackbackend.workout.entity.WorkoutPlan;
import org.dewqryp.fittrackbackend.workoutsession.dto.WorkoutSessionCreateRequest;
import org.dewqryp.fittrackbackend.workoutsession.dto.WorkoutSessionResponse;
import org.dewqryp.fittrackbackend.workoutsession.dto.WorkoutSessionUpdateRequest;
import org.dewqryp.fittrackbackend.workoutsession.entity.WorkoutSession;
import org.dewqryp.fittrackbackend.workoutsession.exceptions.WorkoutSessionIsAlreadyFinishedException;
import org.dewqryp.fittrackbackend.workoutsession.service.WorkoutSessionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkoutSessionController.class)
public class WorkoutSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private WorkoutSessionService workoutSessionService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;


    private User createUser(){
        User user = new User("User", "User", "user@example.com", "hashed-password");
        user.setId(UUID.randomUUID());
        return user;
    }


    @Test
    void shouldCreateWorkoutSession() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        User user = createUser();
        WorkoutSessionCreateRequest workoutSessionCreateRequest = new WorkoutSessionCreateRequest(workoutPlanId, "Notes");
        WorkoutSessionResponse response = new WorkoutSessionResponse(UUID.randomUUID(), workoutPlanId, OffsetDateTime.now(), null, workoutSessionCreateRequest.notes());
        when(workoutSessionService.startWorkoutSession(
                any(WorkoutSessionCreateRequest.class),
                any(User.class)
        )).thenReturn(response);
        mockMvc.perform(post("/api/v1/workout-sessions").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(workoutSessionCreateRequest))
                .with(authentication(new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
        verify(workoutSessionService).startWorkoutSession(any(WorkoutSessionCreateRequest.class), any(User.class));


    }

    @Test
    void shouldReturnAllWorkoutSessions() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        User user = createUser();
        WorkoutSessionResponse response = new WorkoutSessionResponse(UUID.randomUUID(), workoutPlanId, OffsetDateTime.now(), null, "Notes1");
        WorkoutSessionResponse response2 = new WorkoutSessionResponse(UUID.randomUUID(), workoutPlanId, OffsetDateTime.now(), null, "Notes1");
        when(workoutSessionService.getAllWorkoutSessions(any(User.class))).thenReturn(List.of(response,response2));
        mockMvc.perform(get("/api/v1/workout-sessions").with(authentication(new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(response.id().toString()))
                .andExpect(jsonPath("$[1].id").value(response2.id().toString()));
        verify(workoutSessionService).getAllWorkoutSessions(any(User.class));

    }

    @Test
    void shouldReturnWorkoutSession() throws Exception {
        UUID workoutPlanId = UUID.randomUUID();
        UUID workoutSessionId = UUID.randomUUID();
        User user = createUser();
        WorkoutSessionResponse response = new WorkoutSessionResponse(workoutSessionId, workoutPlanId, OffsetDateTime.now(), null, "Notes");
        when(workoutSessionService.getWorkoutSessionById(eq(workoutSessionId), any(User.class))).thenReturn(Optional.of(response));
        mockMvc.perform(get("/api/v1/workout-sessions/{id}", workoutSessionId).with(authentication(new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.workoutPlanId").value(workoutPlanId.toString()))
                .andExpect(jsonPath("$.notes").value(response.notes()));

        verify(workoutSessionService).getWorkoutSessionById(eq(workoutSessionId), any(User.class));

    }

    @Test
    void shouldReturn404WhenWorkoutSessionNotFound() throws Exception {
        UUID workoutSessionId = UUID.randomUUID();
        User user = createUser();
        when(workoutSessionService.getWorkoutSessionById(eq(workoutSessionId), any(User.class))).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/workout-sessions/{id}", workoutSessionId).with(authentication(new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isNotFound());

        verify(workoutSessionService).getWorkoutSessionById(eq(workoutSessionId), any(User.class));
    }

    @Test
    void shouldReturn404WhenWorkoutSessionBelongsToAnotherUser() throws Exception {
        UUID workoutSessionId = UUID.randomUUID();

        User currentUser = createUser();
        User sessionOwner = createUser();

        when(workoutSessionService.getWorkoutSessionById(
                eq(workoutSessionId),
                any(User.class)
        )).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/workout-sessions/{id}", workoutSessionId)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        currentUser,
                                        null,
                                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                )
                        )))
                .andExpect(status().isNotFound());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(workoutSessionService).getWorkoutSessionById(
                eq(workoutSessionId),
                userCaptor.capture()
        );

        assertNotEquals(sessionOwner.getId(), userCaptor.getValue().getId());
    }

    @Test
    void shouldUpdateWorkoutSession() throws Exception {
        UUID sessionId = UUID.randomUUID();
        User user = createUser();

        WorkoutSessionUpdateRequest request =
                new WorkoutSessionUpdateRequest("Updated notes");

        WorkoutSessionResponse response = new WorkoutSessionResponse(
                sessionId,
                null,
                OffsetDateTime.now(),
                null,
                "Updated notes"
        );

        when(workoutSessionService.updateWorkoutSession(
                eq(sessionId),
                any(WorkoutSessionUpdateRequest.class),
                any(User.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/v1/workout-sessions/{id}", sessionId)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        user, null,
                                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                )
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId.toString()))
                .andExpect(jsonPath("$.notes").value("Updated notes"));

        verify(workoutSessionService).updateWorkoutSession(
                eq(sessionId),
                any(WorkoutSessionUpdateRequest.class),
                any(User.class)
        );
    }

    @Test
    void shouldThrow404WhenWorkoutSessionNotFound() throws Exception {
        UUID sessionId = UUID.randomUUID();
        User user = createUser();

        WorkoutSessionUpdateRequest request =
                new WorkoutSessionUpdateRequest("Updated notes");

        when(workoutSessionService.updateWorkoutSession(
                eq(sessionId),
                any(WorkoutSessionUpdateRequest.class),
                any(User.class)
        )).thenThrow(new EntityNotFoundException("Workout session not found"));

        mockMvc.perform(put("/api/v1/workout-sessions/{id}", sessionId)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        user, null,
                                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                )
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409WhenWorkoutSessionIsAlreadyFinished() throws Exception {
        UUID sessionId = UUID.randomUUID();
        User user = createUser();

        WorkoutSessionUpdateRequest request =
                new WorkoutSessionUpdateRequest("Updated notes");

        when(workoutSessionService.updateWorkoutSession(
                eq(sessionId),
                any(WorkoutSessionUpdateRequest.class),
                any(User.class)
        )).thenThrow(new WorkoutSessionIsAlreadyFinishedException(
                "Workout session has already been finished"
        ));

        mockMvc.perform(put("/api/v1/workout-sessions/{id}", sessionId)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        user, null,
                                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                )
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }


}
