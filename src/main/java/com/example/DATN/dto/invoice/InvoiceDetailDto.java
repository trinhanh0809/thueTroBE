package com.example.DATN.dto.invoice;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record InvoiceDetailDto(
        Long id,
        Long contractId,
        String roomTitle,
        Integer tenantId,
        String tenantUsername,
        LocalDate period,
        LocalDate dueDate,
        String status,
        BigDecimal subTotal,
        BigDecimal total,
        Instant issuedAt,
        Instant paidAt,
        String note,
        List<InvoiceItemDto> items
) {}
