package com.example.DATN.dto.room;

import java.math.BigDecimal;
import java.util.List;

public record RoomDetailDto(
        Long id,
        String title,
        String description,
        String addressLine,
        BigDecimal priceMonth,
        BigDecimal deposit,
        BigDecimal electricityPrice,
        BigDecimal waterPrice,
        Integer areaSqm,
        Integer maxOccupancy,
        String status,
        Double lat,
        Double lng,
        RoomTypeDto roomType,
        List<String> amenities,     // tên tiện ích
        List<String> imageUrls,     // url ảnh
        String wardName,
        String districtName,
        String provinceName
) {}
