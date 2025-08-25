package com.example.DATN.dto.user;

import java.time.Instant;
import java.util.List;

public record UserResponse(
        Integer id,
        String username,
        String email,
        String fullName,
        String phoneNumber,
        String avatar,
        boolean enabled,
        boolean host,
        List<String> roles,
        Instant createdAt
) {}
