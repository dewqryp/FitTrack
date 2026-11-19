
package org.dewqryp.fittrackbackend.exercise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dewqryp.fittrackbackend.auth.security.JwtAuthenticationFilter;
import org.dewqryp.fittrackbackend.exercise.dto.ExerciseCreateRequest;
import org.dewqryp.fittrackbackend.exercise.dto.ExerciseResponse;
import org.dewqryp.fittrackbackend.exercise.service.ExerciseService;
import org.dewqryp.fittrackbackend.user.entity.Role;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ExerciseController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(ExerciseControllerTest.TestSecurityConfig.class)
class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ExerciseService exerciseService;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @RestControllerAdvice
        static class TestExceptionHandler {

            @ExceptionHandler(AccessDeniedException.class)
            ResponseEntity<Void> handleAccessDenied() {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
    }

    private User createUser(Role role) {
        User user = new User(
                "User",
                "User",
                "user@example.com",
                "hashed-password"
        );
        user.setRole(role);
        return user;
    }

    private void authenticate(Role role) {
        User user = createUser(role);

        var authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnListOfExercisesForAuthenticatedUser() throws Exception {
        authenticate(Role.USER);

        UUID exerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ExerciseResponse response = new ExerciseResponse(
                exerciseId,
                "Test",
                "Test",
                "Test",
                "Test",
                userId
        );

        when(exerciseService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/exercises"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(exerciseId.toString()))
                .andExpect(jsonPath("$[0].name").value("Test"));

        verify(exerciseService).findAll();
    }

    @Test
    void shouldReturnExerciseById() throws Exception {
        authenticate(Role.USER);

        UUID exerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ExerciseResponse response = new ExerciseResponse(
                exerciseId,
                "Test",
                "Test",
                "Test",
                "Test",
                userId
        );

        when(exerciseService.findById(exerciseId))
                .thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/v1/exercises/{id}", exerciseId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(exerciseId.toString()))
                .andExpect(jsonPath("$.name").value("Test"));

        verify(exerciseService).findById(exerciseId);
    }

    @Test
    void shouldReturn404WhenExerciseDoesNotExist() throws Exception {
        authenticate(Role.USER);

        UUID exerciseId = UUID.randomUUID();

        when(exerciseService.findById(exerciseId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/exercises/{id}", exerciseId))
                .andExpect(status().isNotFound());

        verify(exerciseService).findById(exerciseId);
    }

    @Test
    void shouldCreateExerciseForTrainer() throws Exception {
        User trainer = createUser(Role.TRAINER);
        authenticate(Role.TRAINER);

        ExerciseCreateRequest request = new ExerciseCreateRequest(
                "Bench Press",
                "Chest exercise",
                "Chest",
                "Barbell"
        );

        UUID userId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();

        ExerciseResponse response = new ExerciseResponse(
                exerciseId,
                "Bench Press",
                "Chest exercise",
                "Chest",
                "Barbell",
                userId
        );

        when(exerciseService.create(any(ExerciseCreateRequest.class), any(User.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(exerciseId.toString()))
                .andExpect(jsonPath("$.name").value("Bench Press"));

        verify(exerciseService).create(any(ExerciseCreateRequest.class), any(User.class));
    }

    @Test
    void shouldForbidExerciseCreationForRegularUser() throws Exception {
        authenticate(Role.USER);

        ExerciseCreateRequest request = new ExerciseCreateRequest(
                "Bench Press",
                "Chest exercise",
                "Chest",
                "Barbell"
        );

        mockMvc.perform(post("/api/v1/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(exerciseService);
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        authenticate(Role.TRAINER);

        ExerciseCreateRequest request = new ExerciseCreateRequest(
                "",
                "",
                "Chest",
                "Barbell"
        );

        mockMvc.perform(post("/api/v1/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(exerciseService);
    }
}
