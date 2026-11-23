package org.dewqryp.fittrackbackend.user.service;

import org.dewqryp.fittrackbackend.user.dto.UserCreateRequest;
import org.dewqryp.fittrackbackend.user.dto.UserResponse;
import org.dewqryp.fittrackbackend.user.entity.Role;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.user.exceptions.DuplicateEmailException;
import org.dewqryp.fittrackbackend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;
    @Test
    void shouldRegisterUserWhenEmailAvailable(){
        UUID uuid = UUID.randomUUID();
        UserCreateRequest userCreateRequest = new UserCreateRequest("Test", "Test", "test@example.com", "password");
        when(userRepository.existsByEmail(userCreateRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(userCreateRequest.password())).thenReturn("hashed-password");
        User user = new User(userCreateRequest.firstName(),  userCreateRequest.lastName(), userCreateRequest.email(), "hashed-password");
        user.setId(uuid);
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserResponse userResponse = userService.registerUser(userCreateRequest);
        assertEquals(userCreateRequest.email(), userResponse.email());
        assertEquals(userCreateRequest.firstName(), userResponse.firstName());
        assertEquals(userCreateRequest.lastName(), userResponse.lastName());
        assertEquals(uuid, userResponse.id());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(userCreateRequest.password());


    }

    @Test
    void shouldNotRegisterUserWhenEmailAlreadyExists() {
        UserCreateRequest userCreateRequest = new UserCreateRequest("Test", "Test", "test@example.com", "password");
        when(userRepository.existsByEmail(userCreateRequest.email())).thenReturn(true);
        assertThrows(DuplicateEmailException.class, () -> userService.registerUser(userCreateRequest));
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(userCreateRequest.password());
        verify(userRepository).existsByEmail(userCreateRequest.email());
    }

    @Test
    void shouldGetCurrentUser(){
        UUID userId = UUID.randomUUID();
        User u = new User("User", "User", "user@example.com", "hashed-password");
        u.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(u));
        UserResponse response = userService.findResponseById(userId, u).get();
        assertNotNull(response);
        assertAll(
                () -> assertEquals(u.getFirstName(), response.firstName()),
                () -> assertEquals(u.getLastName(), response.lastName()),
                () -> assertEquals(u.getEmail(), response.email()),
                () -> assertEquals(u.getId(), response.id())
        );
        verify(userRepository).findById(u.getId());

    }

    @Test
    void shouldNotGetSomeoneElseProfile(){
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        User u = new User("User", "User", "user@example.com", "hashed-password");
        u.setId(userId);
        Optional<UserResponse> response = userService.findResponseById(otherUserId, u);
        assertTrue(response.isEmpty());
        verify(userRepository, never()).findById(otherUserId);
    }

    @Test
    void shouldGetSomeoneElseProfileIfRoleIsAdmin(){
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        User u = new User("User", "User", "user@example.com", "hashed-password");
        u.setId(userId);
        User admin = new User("Admin", "Admin", "admin@example.com", "hashed-password");
        admin.setId(adminId);
        admin.setRole(Role.ADMIN);
        when(userRepository.findById(userId)).thenReturn(Optional.of(u));
        UserResponse response = userService.findResponseById(userId, admin).orElse(null);
        assertNotNull(response);
        assertAll(
                () -> assertEquals(u.getFirstName(), response.firstName()),
                () -> assertEquals(u.getLastName(), response.lastName()),
                () -> assertEquals(u.getEmail(), response.email()),
                () -> assertEquals(u.getId(), response.id())
        );
        verify(userRepository).findById(userId);

    }

    @Test
    void shouldReturnEmptyWhenAdminRequestsNonExistingUser(){
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        User admin = new User("Admin", "Admin", "admin@example.com", "hashed-password");
        admin.setId(adminId);
        admin.setRole(Role.ADMIN);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Optional<UserResponse> response = userService.findResponseById(userId, admin);
        assertTrue(response.isEmpty());
        verify(userRepository).findById(userId);
    }
}
