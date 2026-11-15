package org.dewqryp.fittrackbackend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(@NotBlank String firstName, @NotBlank String lastName, @Email @NotBlank String email, @Size(min = 8, max = 50) @NotBlank String password) {
}
