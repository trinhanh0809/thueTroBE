package com.example.DATN.dto.user;

public record UserProfileUpdateRequest(
        String firstName,
        String lastName,
        String phoneNumber,
        String avatar
) {}
