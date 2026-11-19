package org.dewqryp.fittrackbackend.exercise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.dewqryp.fittrackbackend.auth.security.JwtAuthenticationFilter;
import org.dewqryp.fittrackbackend.exercise.dto.ExerciseCreateRequest;
import org.dewqryp.fittrackbackend.exercise.dto.ExerciseResponse;
import org.dewqryp.fittrackbackend.exercise.service.ExerciseService;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.user.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
@AutoConfigureMockMvc(addFilters = false)
@Import(ExerciseControllerTest.TestSecurityConfig.class)
class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ExerciseService exerciseService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnEmptyBodyForUserWithoutExercises() throws Exception {
        authenticate(Role.USER);
        mockMvc.perform(post("/api/v1/exercises")
                        .contentType("application/json")
                        .content(""))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(exerciseService);
    }

    @Test
    void shouldReturnListOfExercisesForAuthenticatedUser() throws Exception {
        authenticate(Role.USER);
        UUID exerciseId = UUID.randomUUID();
        UUID exerciseId1 = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ExerciseResponse response = new ExerciseResponse(exerciseId, "Test", "Test", "Test", "Test", userId);
        ExerciseResponse response1 = new ExerciseResponse(exerciseId1, "Test1", "Test1", "Test1", "Test1", userId);

        when(exerciseService.findAll()).thenReturn(List.of(response, response1));

        mockMvc.perform(get("/api/v1/exercises"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$[0].name").value("Test"))
                .andExpect(jsonPath("$[0].id").value(exerciseId.toString()))
                .andExpect(jsonPath("$[1].name").value("Test1"))
                .andExpect(jsonPath("$[1].id").value(exerciseId1.toString()));

        verify(exerciseService).findAll();
    }


    @Test
    void shouldReturnExercisesForAuthenticatedUser() throws Exception {
        authenticate(Role.USER);
        UUID exerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ExerciseResponse response = new ExerciseResponse(exerciseId, "Test", "Test", "Test", "Test", userId);

        when(exerciseService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/exercises"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$[0].name").value("Test"))
                .andExpect(jsonPath("$[0].id").value(exerciseId.toString()));

        verify(exerciseService).findAll();
    }

    @Test
    void shouldReturnExerciseById() throws Exception {
        authenticate(Role.USER);

        UUID exerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ExerciseResponse response = new ExerciseResponse(exerciseId, "Test", "Test", "Test", "Test", userId);


        when(exerciseService.findById(exerciseId)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/v1/exercises/{id}", exerciseId))
                .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.id").value(exerciseId.toString()));

        verify(exerciseService).findById(exerciseId);
    }

    @Test
    void shouldReturn404WhenExerciseDoesNotExist() throws Exception {
        authenticate(Role.USER);

        UUID id = UUID.randomUUID();

        when(exerciseService.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/exercises/{id}", id))
                .andExpect(status().isNotFound());

        verify(exerciseService).findById(id);
    }

    @Test
    void shouldCreateExerciseForTrainer() throws Exception {
        authenticate(Role.TRAINER);

        ExerciseCreateRequest request = new ExerciseCreateRequest(
                "Bench Press",
                "Chest exercise",
                "Chest",
                "Barbell"
        );

        UUID userId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();

        ExerciseResponse response = new ExerciseResponse(exerciseId, "Bench Press", "Chest exercise", "Chest", "Barbell", userId);

        when(exerciseService.create(any(ExerciseCreateRequest.class), any(User.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/exercises")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.name").value("Bench Press"))
                .andExpect(jsonPath("$.id").value(exerciseId.toString()));

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

        ServletException servletException = assertThrows(ServletException.class, () -> mockMvc.perform(post("/api/v1/exercises")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))));
        assertInstanceOf(
                AuthorizationDeniedException.class,
                servletException.getCause()
        );
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
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(exerciseService);
    }



    private void authenticate(Role role) {
        User user = mock(User.class);
        var authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableMethodSecurity
    static class TestSecurityConfig {
    }
}