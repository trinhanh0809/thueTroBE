package com.example.DATN.dto.roomType;

import com.example.DATN.entity.RoomType;

public record RoomTypeDto(Long id, String code, String name, Integer sortOrder) {
    public static RoomTypeDto from(RoomType r) {
        return new RoomTypeDto(r.getId(), r.getCode(), r.getName(), r.getSortOrder());
    }
}