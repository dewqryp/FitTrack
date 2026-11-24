package org.dewqryp.fittrackbackend.user.controller;

import jakarta.validation.Valid;
import org.dewqryp.fittrackbackend.user.dto.UserCreateRequest;
import org.dewqryp.fittrackbackend.user.dto.UserResponse;
import org.dewqryp.fittrackbackend.user.dto.UserUpdateRequest;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findResponseById(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.of(userService.findResponseById(id, user));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        UserResponse response = userService.registerUser(userCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.of(userService.findResponseById(u.getId(), u));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest userUpdateRequest, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.updateProfile(id, userUpdateRequest, user));
    }

}
