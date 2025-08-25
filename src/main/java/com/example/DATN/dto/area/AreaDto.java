package com.example.DATN.dto.area;

import com.example.DATN.enums.AreaType;

public record AreaDto(
        Long id,
        String name,
        String code,
        AreaType type,
        Long parentId
) {}
