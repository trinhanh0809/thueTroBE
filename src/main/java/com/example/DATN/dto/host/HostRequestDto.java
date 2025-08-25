package com.example.DATN.dto.host;

import java.time.Instant;

public record HostRequestDto(
        Long id,
        Integer userId,
        String username,
        String status,        // PENDING/APPROVED/REJECTED
        String note,
        String reason,
        Instant createdAt,
        Instant approvedAt,
        Integer approvedBy
) {}
