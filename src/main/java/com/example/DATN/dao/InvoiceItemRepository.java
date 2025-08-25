package com.example.DATN.dao;

import com.example.DATN.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    List<InvoiceItem> findByInvoice_Id(Long invoiceId);
}
