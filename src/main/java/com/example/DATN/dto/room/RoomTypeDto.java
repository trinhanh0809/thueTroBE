package com.example.DATN.dto.room;

public record RoomTypeDto(
        Long id,
        String code,
        String name,
        Integer sortOrder
) {}
