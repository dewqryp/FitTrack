package org.dewqryp.fittrackbackend.user.controller;

import org.dewqryp.fittrackbackend.user.dto.UserCreateRequest;
import org.dewqryp.fittrackbackend.user.dto.UserResponse;
import org.dewqryp.fittrackbackend.user.service.UserService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @MockitoBean
    UserService userService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSuccessfullyRegisterUser() throws Exception {
        UUID uuid = UUID.randomUUID();
        UserCreateRequest userCreateRequest = new UserCreateRequest("Test", "Test", "test@example.com",  "password");
        UserResponse response = new UserResponse(uuid, userCreateRequest.firstName(), userCreateRequest.lastName(), userCreateRequest.email());
        when(userService.registerUser(any(UserCreateRequest.class))).thenReturn(response);
        String json = objectMapper.writeValueAsString(userCreateRequest);
        mockMvc.perform(post("/api/v1/users").contentType("application/json").content(json)).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(uuid.toString()))
                .andExpect(jsonPath("$.email").value(userCreateRequest.email()))
                .andExpect(jsonPath("$.firstName").value(userCreateRequest.firstName()))
                .andExpect(jsonPath("$.lastName").value(userCreateRequest.lastName()))
                .andExpect(jsonPath("$.password").doesNotExist()).andExpect(jsonPath("$.passwordHash").doesNotExist());
        verify(userService).registerUser(any(UserCreateRequest.class));

    }

    @Test
    void shouldRejectRegistrationWhenFirstNameIsBlank() throws Exception {
        UserCreateRequest userCreateRequest = new UserCreateRequest("", "Test", "test@example.com",  "password");
        String json = objectMapper.writeValueAsString(userCreateRequest);
        mockMvc.perform(post("/api/v1/users").contentType("application/json").content(json)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("firstName")));
        verify(userService, never()).registerUser(any(UserCreateRequest.class));

    }
    @Test
    void shouldRejectRegistrationWithInvalidEmail() throws Exception {
        UserCreateRequest userCreateRequest = new UserCreateRequest("Test", "Test", "test.com",  "password");
        String json = objectMapper.writeValueAsString(userCreateRequest);
        mockMvc.perform(post("/api/v1/users").contentType("application/json").content(json)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("email")));
        verify(userService, never()).registerUser(any(UserCreateRequest.class));

    }
}
