
package com.example.DATN.dto.room;

import java.math.BigDecimal;
import java.util.List;

public record UpdateRoomRequest(
        Long blockId,
        Long wardId,
        Long roomTypeId,
        String addressLine,
        String title,
        String description,
        BigDecimal priceMonth,
        BigDecimal deposit,
        BigDecimal electricityPrice,
        BigDecimal waterPrice,
        Integer areaSqm,
        Integer maxOccupancy,
        Double lat,
        Double lng,
        List<Long> amenityIds,
        List<String> imageUrls
) {}
