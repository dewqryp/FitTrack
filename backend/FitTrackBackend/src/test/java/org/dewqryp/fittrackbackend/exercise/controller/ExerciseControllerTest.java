package org.dewqryp.fittrackbackend.exercise.controller;

import org.dewqryp.fittrackbackend.exercise.dto.ExerciseCreateRequest;
import org.dewqryp.fittrackbackend.exercise.dto.ExerciseResponse;
import org.dewqryp.fittrackbackend.exercise.service.ExerciseService;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.dewqryp.fittrackbackend.user.entity.Role;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

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

    @Test
    void shouldCreateExerciseForTrainer() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        User trainer = new User("Test", "Test", "trainer@example.com", "hashed-password");
        trainer.setId(userId);
        trainer.setRole(Role.TRAINER);
        ExerciseCreateRequest exerciseCreateRequest = new ExerciseCreateRequest("Test", "Test", "Test", "Test");
        ExerciseResponse exerciseResponse = new ExerciseResponse(exerciseId, "Name", "Test", "Test", "Test", UUID.randomUUID());
        when(exerciseService.create(exerciseCreateRequest, trainer)).thenReturn(exerciseResponse);
        mockMvc.perform(post("/api/v1/exercises").with(authenticationOf(trainer, "ROLE_TRAINER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(exerciseCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.createdBy").value(userId.toString()));

    }

    @Test
    void shouldRejectExerciseCreationForUser() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User("Test", "Test", "trainer@example.com", "hashed-password");
        user.setId(userId);
        ExerciseCreateRequest exerciseCreateRequest = new ExerciseCreateRequest("Test", "Test", "Test", "Test");
        mockMvc.perform(post("/api/v1/exercises").with(authenticationOf(user, "ROLE_USER"))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(exerciseCreateRequest)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(exerciseService);

    }

    @Test
    void shouldRejectInvalidExerciseRequest() throws Exception {
        UUID userId = UUID.randomUUID();
        User trainer = new User("Test", "Test", "trainer@example.com", "hashed-password");
        trainer.setId(userId);
        trainer.setRole(Role.TRAINER);
        String invalidJson = """
            {
              "name": "",
              "description": "",
              "muscleGroup": ""
            }
            """;
        mockMvc.perform(post("/api/v1/exercises").with(authenticationOf(trainer, "ROLE_TRAINER"))
                .contentType(MediaType.APPLICATION_JSON).content(invalidJson)).andExpect(status().isBadRequest());
        verifyNoInteractions(exerciseService);
        
    }



    private RequestPostProcessor authenticationOf(User user, String role){
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority(role)));
        return authentication(authentication);
    }

}
