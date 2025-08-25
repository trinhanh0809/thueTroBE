package com.example.DATN.dto.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceSummaryDto(
        Long id,
        Long contractId,
        String roomTitle,
        Integer tenantId,
        String tenantUsername,
        LocalDate period,
        LocalDate dueDate,
        String status,
        BigDecimal total
) {}