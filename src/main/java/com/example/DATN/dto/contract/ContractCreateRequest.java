package com.example.DATN.dto.contract;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractCreateRequest(
        Long roomId,
        Integer tenantId,
        LocalDate startDate,
        Integer billDay,
        BigDecimal priceMonth,
        BigDecimal elecPrice,
        BigDecimal waterPrice
) {}