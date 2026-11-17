package org.dewqryp.fittrackbackend.exercise.controller;

import org.dewqryp.fittrackbackend.exercise.dto.ExerciseCreateRequest;
import org.dewqryp.fittrackbackend.exercise.dto.ExerciseResponse;
import org.dewqryp.fittrackbackend.exercise.service.ExerciseService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExerciseController.class)
public class ExerciseControllerTest {

    @MockitoBean
    private ExerciseService exerciseService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldReturnAllExercisesForAuthenticatedUser() throws Exception {
        UUID uuid = UUID.randomUUID();
        ExerciseResponse response = new ExerciseResponse(uuid, "Name", "Test", "Test", "Test", UUID.randomUUID());
        when(exerciseService.findAll()).thenReturn(List.of(response));
        mockMvc.perform(get("/api/v1/exercises")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(uuid.toString()))
                .andExpect(jsonPath("$[0].name").value("Name"));
    }

    @Test
    @WithMockUser
    void shouldReturnExerciseById() throws Exception {
        UUID uuid = UUID.randomUUID();
        ExerciseResponse response = new ExerciseResponse(uuid, "Name", "Test", "Test", "Test", UUID.randomUUID());
        when(exerciseService.findById(uuid)).thenReturn(Optional.of(response));
        mockMvc.perform(get("/api/v1/exercises/" + uuid)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(uuid.toString()))
                .andExpect(jsonPath("$.name").value("Name"));
    }

    @Test
    @WithMockUser
    void shouldReturn404WhenExerciseNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(exerciseService.findById(uuid)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/exercises/" + uuid)).andExpect(status().isNotFound());
    }



}
