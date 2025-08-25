package com.example.DATN.dto.room;

import java.math.BigDecimal;

public record RoomListDto(
        Long id,
        String title,
        String coverImageUrl,
        BigDecimal priceMonth,
        Integer areaSqm,
        String wardName,
        String districtName,
        String provinceName
) {}