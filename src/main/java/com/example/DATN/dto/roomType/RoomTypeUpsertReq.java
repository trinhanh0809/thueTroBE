package com.example.DATN.dto.roomType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoomTypeUpsertReq(
        @NotBlank @Size(max = 32)  String code,
        @NotBlank @Size(max = 128) String name,
        Integer sortOrder
) {}
