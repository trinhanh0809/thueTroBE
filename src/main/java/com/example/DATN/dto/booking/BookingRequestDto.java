package com.example.DATN.dto.booking;

import java.time.Instant;
import java.time.LocalDateTime;

public record BookingRequestDto(
        Long id,
        Long roomId,
        String roomTitle,
        Integer tenantId,
        String tenantUsername,
        String status,
        LocalDateTime scheduleAt,
        String note,
        Instant createdAt
) {}
