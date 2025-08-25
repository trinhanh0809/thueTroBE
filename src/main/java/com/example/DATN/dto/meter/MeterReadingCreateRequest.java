package com.example.DATN.dto.meter;

import java.time.LocalDate;

public record MeterReadingCreateRequest(
        Long contractId,
        LocalDate period, // YYYY-MM-01
        Integer elecPrev,
        Integer elecCurr,
        Integer waterPrev,
        Integer waterCurr
) {}