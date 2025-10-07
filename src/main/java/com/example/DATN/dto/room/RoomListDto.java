// com/example/DATN/dto/room/RoomListDto.java
package com.example.DATN.dto.room;

import java.math.BigDecimal;
import java.util.List;

public record RoomListDto(
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

        // >>> Thêm mới:
        Long roomTypeId,
        String roomTypeName,
        Long blockId,

        // giữ nguyên các field còn lại
        List<String> amenityNames,
        List<String> imageUrls,
        String wardName,
        String districtName,
        String provinceName
) {}
