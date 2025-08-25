package com.example.DATN.dto.contract;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractDto(
        Long id,
        Long roomId,
        String roomTitle,
        Integer tenantId,
        String tenantUsername,
        LocalDate startDate,
        LocalDate endDate,
        Integer billDay,
        BigDecimal priceMonth,
        BigDecimal elecPrice,
        BigDecimal waterPrice,
        Boolean active
) {}
