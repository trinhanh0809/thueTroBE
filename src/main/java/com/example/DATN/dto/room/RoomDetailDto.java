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

        // room type (đúng kiểu)
        Long roomTypeId,
        String roomTypeName,

        // block
        Long blockId,

        // amenities
        List<Long> amenityIds,

        // images
        List<String> imageUrls,

        // location
        Long wardId,
        Long districtId,
        Long provinceId,

        ContactDto contact
) {}
