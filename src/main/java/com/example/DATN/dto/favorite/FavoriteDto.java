package com.example.DATN.dto.favorite;

import java.time.Instant;

public record FavoriteDto(
        Long roomId,
        String roomTitle,
        String coverImageUrl,
        Instant createdAt
) {}
