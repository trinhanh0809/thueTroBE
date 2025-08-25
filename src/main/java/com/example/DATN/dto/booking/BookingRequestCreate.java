package com.example.DATN.dto.booking;

import java.time.LocalDateTime;

public record BookingRequestCreate(
        Long roomId,
        LocalDateTime scheduleAt,
        String note
) {}
