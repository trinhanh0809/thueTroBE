package com.example.DATN.dto.amenity;

import com.example.DATN.entity.Amenity;

public record AmenityDto(Long id, String code, String name, Integer sortOrder) {
    public static com.example.DATN.dto.amenity.AmenityDto from(Amenity r) {
        return new com.example.DATN.dto.amenity.AmenityDto(r.getId(), r.getCode(), r.getName(), r.getSortOrder());
    }
}
