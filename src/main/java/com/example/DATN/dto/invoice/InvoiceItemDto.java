package com.example.DATN.dto.invoice;

import java.math.BigDecimal;

public record InvoiceItemDto(
        Long id,
        String name,
        BigDecimal qty,
        BigDecimal unitPrice,
        BigDecimal amount
) {}
