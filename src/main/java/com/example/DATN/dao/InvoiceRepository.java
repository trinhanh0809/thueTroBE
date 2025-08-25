package com.example.DATN.dao;

import com.example.DATN.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByContract_IdAndPeriod(Long contractId, LocalDate period);
    List<Invoice> findByContract_Room_Host_IdUserAndPeriod(Integer hostId, LocalDate period);
    List<Invoice> findByContract_Room_Id(Long roomId);
}
