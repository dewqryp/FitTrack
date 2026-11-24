package org.dewqryp.fittrackbackend.user.controller;

import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.auth.security.JwtAuthenticationFilter;
import org.dewqryp.fittrackbackend.user.dto.UserCreateRequest;
import org.dewqryp.fittrackbackend.user.dto.UserResponse;
import org.dewqryp.fittrackbackend.user.dto.UserUpdateRequest;
import org.dewqryp.fittrackbackend.user.entity.Role;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.user.exceptions.DuplicateEmailException;
import org.dewqryp.fittrackbackend.user.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
))
@Import(UserControllerTest.TestSecurityConfig.class)
public class UserControllerTest {
    @MockitoBean
    UserService userService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
    }

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
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

    private User authenticate(Role role) {
        User user = createUser(role);

        var authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return user;
    }

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
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("firstName")));
        verify(userService, never()).registerUser(any(UserCreateRequest.class));

    }
    @Test
    void shouldRejectRegistrationWithInvalidEmail() throws Exception {
        UserCreateRequest userCreateRequest = new UserCreateRequest("Test", "Test", "test.com",  "password");
        String json = objectMapper.writeValueAsString(userCreateRequest);
        mockMvc.perform(post("/api/v1/users").contentType("application/json").content(json)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("email")));
        verify(userService, never()).registerUser(any(UserCreateRequest.class));

    }
    @Test
    void shouldRejectRegistrationWithEmailThatAlreadyExists() throws Exception {
        UserCreateRequest userCreateRequest = new UserCreateRequest("Test", "Test", "test@example.com",  "password");
        String json = objectMapper.writeValueAsString(userCreateRequest);
        when(userService.registerUser(any(UserCreateRequest.class))).thenThrow(new DuplicateEmailException("Email already exists"));
        mockMvc.perform(post("/api/v1/users").contentType("application/json").content(json)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString    ("Email already exists")));
        verify(userService).registerUser(any(UserCreateRequest.class));

    }

    @Test
    void shouldUpdateOwnProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = authenticate(Role.USER);
        user.setId(userId);
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest("updated", "updated", "updated@example.com");
        UserResponse response = new UserResponse(userId, userUpdateRequest.firstName(), userUpdateRequest.lastName(), userUpdateRequest.email());
        when(userService.updateProfile(eq(userId), any(UserUpdateRequest.class), any(User.class))).thenReturn(response);
        mockMvc.perform(put("/api/v1/users/{id}", userId).contentType("application/json").content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(userUpdateRequest.firstName()))
                .andExpect(jsonPath("$.lastName").value(userUpdateRequest.lastName()))
                .andExpect(jsonPath("$.email").value(userUpdateRequest.email()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldNotUpdateOtherUserProfile() throws Exception {
        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();
        User alice = authenticate(Role.USER);
        alice.setId(aliceId);
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest("updated", "updated", "updated@example.com");
        doThrow(new EntityNotFoundException("User not found"))
                .when(userService)
                .updateProfile(
                        eq(bobId),
                        any(UserUpdateRequest.class),
                        any(User.class)
                );
        mockMvc.perform(put("/api/v1/users/{id}", bobId).contentType("application/json").content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isNotFound());
        verify(userService).updateProfile(
                eq(bobId),
                eq(userUpdateRequest),
                any(User.class)
        );
    }

    @Test
    void shouldNotUpdateNonExistentUser() throws Exception {
        User user = authenticate(Role.USER);
        UUID userId = UUID.randomUUID();
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest("Updated", "Updated", "updated@example.com");
        doThrow(new EntityNotFoundException("User not found")).when(userService).updateProfile(eq(userId), any(UserUpdateRequest.class), any(User.class));
        mockMvc.perform(put("/api/v1/users/{id}", userId).contentType("application/json").content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isNotFound());
        verify(userService).updateProfile(
                eq(userId),
                eq(userUpdateRequest),
                any(User.class)
        );
    }

    @Test
    void shouldNotUpdateProfileWithInvalidData() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = authenticate(Role.USER);
        user.setId(userId);
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest(null, "updated", "updated@example.com");
        mockMvc.perform(put("/api/v1/users/{id}", userId).contentType("application/json").content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isBadRequest());
        verify(userService, never()).updateProfile(any(UUID.class), any(UserUpdateRequest.class), any(User.class));
    }



}
