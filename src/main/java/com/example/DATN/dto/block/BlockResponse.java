package com.example.DATN.dto.block;

import java.time.LocalDateTime;

public record BlockResponse(
        Long id,
        String name,
        String address,
        String coverImageUrl,
        boolean enable,
        LocalDateTime createdAt,
        int count_room
) {}

