package com.example.DATN.dto.meter;

import java.time.LocalDate;

public record MeterReadingDto(
        Long id,
        Long contractId,
        LocalDate period,
        Integer elecPrev,
        Integer elecCurr,
        Integer waterPrev,
        Integer waterCurr
) {}
