package org.dewqryp.fittrackbackend.user.service;

import jakarta.persistence.EntityNotFoundException;
import org.dewqryp.fittrackbackend.user.dto.UserCreateRequest;
import org.dewqryp.fittrackbackend.user.dto.UserResponse;
import org.dewqryp.fittrackbackend.user.dto.UserUpdateRequest;
import org.dewqryp.fittrackbackend.user.entity.Role;
import org.dewqryp.fittrackbackend.user.entity.User;
import org.dewqryp.fittrackbackend.user.exceptions.DuplicateEmailException;
import org.dewqryp.fittrackbackend.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
    }

    public Optional<UserResponse> findResponseById(UUID id, User user) {
        if(id.equals(user.getId()) || user.getRole() == Role.ADMIN) {
            return userRepository.findById(id).map(this::toResponse);
        }
        return Optional.empty();
    }

    @Transactional
    public UserResponse registerUser(UserCreateRequest userCreateRequest) {

        if(userRepository.existsByEmail(userCreateRequest.email())){
            throw new DuplicateEmailException("Email already exists");
        }


        User newUser = userRepository.save(new User(userCreateRequest.firstName(), userCreateRequest.lastName(), userCreateRequest.email(), passwordEncoder.encode(userCreateRequest.password())));
        return new UserResponse(newUser.getId(), newUser.getFirstName(), newUser.getLastName(), newUser.getEmail());

    }

    @Transactional
    public UserResponse updateProfile(UUID id, UserUpdateRequest userUpdateRequest, User currentUser) {
        if(id.equals(currentUser.getId())) {
            User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));

            if(!userRepository.existsByEmailAndIdNot(userUpdateRequest.email(), id)) {
                user.setFirstName(userUpdateRequest.firstName());
                user.setLastName(userUpdateRequest.lastName());
                user.setEmail(userUpdateRequest.email());
                return toResponse(userRepository.save(user));
            }
            throw new DuplicateEmailException("Email already exists");


        }

        throw new EntityNotFoundException("User not found");

    }
}
