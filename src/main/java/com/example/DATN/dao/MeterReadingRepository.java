package com.example.DATN.dao;

import com.example.DATN.entity.Contract;
import com.example.DATN.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {
    Optional<MeterReading> findByContract_IdAndPeriod(Long contractId, LocalDate period);
    List<MeterReading> findByContract(Contract contract);
    Optional<MeterReading> findTop1ByContract_IdOrderByPeriodDesc(Long contractId);
}
