package com.example.DATN.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 6, max = 128) String password,
        @NotBlank @Email String email,
        String firstName,
        String lastName,
        String phoneNumber
) {}
