package com.example.DATN.dto.invoice;


public record InvoiceStatusUpdateRequest(
        String status,   // PAID / CANCELED
        String note
) {}
